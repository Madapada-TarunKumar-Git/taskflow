package com.example.taskflow.application.service;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.dto.TaskProcessingResultDto;
import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.exception.InvalidTaskStateException;
import com.example.taskflow.presentation.response.CreateTaskResponseDto;
import com.example.taskflow.presentation.response.RetryTaskResponseDto;
import com.example.taskflow.application.mapper.TaskMapper;
import com.example.taskflow.application.port.FileStoragePort;
import com.example.taskflow.application.port.FileUploadResult;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.application.validation.TaskFileValidator;
import com.example.taskflow.domain.exception.TaskNotFoundException;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import com.example.taskflow.presentation.response.UploadResponseDto;
import com.example.taskflow.shared.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TaskCommandService implements TaskCommandUseCase {
    private final TaskRepository taskRepository;
    private final FileStoragePort fileStoragePort;
    private final TaskFileValidator taskFileValidator;
    private final CustomerCsvProcessingService customerCsvProcessingService;
    private final TaskAuditService taskAuditService;
    private final SecurityUtil securityUtil;

    @Override
    public CreateTaskResponseDto createTask(CreateTaskCommand command) {
        log.info("Creating task. name = {}, type = {}, priority = {}, user = {}",
                command.taskName(), command.taskType(), command.priority(), securityUtil.getUsername());
        Task task = new Task(
                command.taskName(),
                command.description(),
                command.taskType(),
                command.priority(),
                securityUtil.getUsername()
        );

        Task savedTask = taskRepository.save(task);
        taskAuditService.logTaskEvent(savedTask, TaskAuditAction.TASK_CREATED,
                TaskStatus.CREATED, savedTask.getStatus(),
                "Task created",
                securityUtil.getUsername()
        );

        log.info("Task created successfully. taskId = {}, status = {}, createdBy = {}",
                savedTask.getId(), savedTask.getStatus(), securityUtil.getUsername());
        return TaskMapper.toCreateTaskResponseDto(savedTask);
    }

    @Override
    public UploadResponseDto uploadTask(UploadTaskCommand command, MultipartFile file) {
        log.info("Starting upload  task. name = {}, type = {}, priority =  {}, filename = {}, user = {}",
                command.taskName(), command.taskType(), command.priority(), file.getName(), securityUtil.getUsername());
        log.debug("Validating uploaded file {}.", file.getName());
        taskFileValidator.validate(file, command.taskType());
        log.debug("File validation completed successfully. file = {}", file.getOriginalFilename());

        log.info("Storing uploaded file. originalFilename = {}", file.getName());
        FileUploadResult fileUploadResult = fileStoragePort.storeFile(file);
        log.info("File stored successfully. storedFilename = {}, size = {} bytes", fileUploadResult.storedFilename(), fileUploadResult.fileSize());

        Task task = new Task(
                command.taskName(),
                command.description(),
                command.taskType(),
                command.priority(),
                securityUtil.getUsername()
        );

        task.setOriginalFileName(fileUploadResult.originalFileName());
        task.setStoredFileName(fileUploadResult.storedFilename());
        task.setFilePath(fileUploadResult.filePath());
        task.setFileSize(fileUploadResult.fileSize());

        TaskStatus beforeFileUpload = task.getStatus();
        task.markAsFileUploaded();

        TaskStatus beforeQueue = task.getStatus();
        task.markAsQueued();

        Task savedTask = taskRepository.save(task);

        taskAuditService.logTaskEvent(savedTask, TaskAuditAction.FILE_UPLOADED, beforeFileUpload,
                TaskStatus.FILE_UPLOADED, "File Uploaded", securityUtil.getUsername());
        taskAuditService.logTaskEvent(savedTask, TaskAuditAction.TASK_QUEUED, beforeQueue,
                TaskStatus.QUEUED, "Task Queued for processing", securityUtil.getUsername());

        log.info("Task queued successfully. taskId = {}, status = {}", savedTask.getId(), savedTask.getStatus());
        return TaskMapper.toUploadResponseDto(savedTask);
    }

    @Override
    public void processTask(Long taskId) {
        log.info("Starting task processing. taskId = {}", taskId);
        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new TaskNotFoundException("Task not found with id: " + taskId));
        log.debug("Loaded task. taskId = {}, status = {}, retryCount = {}", task.getId(), task.getStatus(), task.getRetryCount());

        try {
            log.info("Processing file for task: {}, filename = {}", taskId, task.getOriginalFileName());
            TaskProcessingResultDto result = customerCsvProcessingService.process(task.getFilePath());
            log.info("Task {} processed successfully. totalRecords = {}, successRecords = {}, failedRecords = {}",
                    task.getId(), result.totalRecords(), result.successfulRecords(), result.failedRecords());
            task.updateProcessingStatistics(
                    result.totalRecords(),
                    result.successfulRecords(),
                    result.failedRecords()
            );

            TaskStatus beforeCompletion = task.getStatus();
            if (result.failedRecords() > 0) {
                task.markAsPartiallyCompleted();
                log.warn("Task {} completed with partial failures. failureRecords = {}", task.getId(), result.failedRecords());
                taskAuditService.logTaskEvent(task, TaskAuditAction.PROCESSING_PARTIALLY_COMPLETED, beforeCompletion, task.getStatus(), "Processing partially completed", "SYSTEM");
            } else {
                task.markAsCompleted();
                log.info("Task {} completed successfully.", task.getId());
                taskAuditService.logTaskEvent(task, TaskAuditAction.TASK_COMPLETED, beforeCompletion, task.getStatus(), "Processing completed", "SYSTEM");
            }
        } catch (Exception ex) {
            TaskStatus beforeFailure = task.getStatus();
            task.incrementRetryCount();
            log.error("Task processing failed. taskId = {}, retryCount = {}", task.getId(), task.getRetryCount(), ex);
            if (task.hasReachedMaxRetryLimit()) {
                task.markAsPermanentFailure(ex.getMessage());
                log.error("Task {} permanently failed after retries {}.", task.getId(), task.getRetryCount());
                taskAuditService.logTaskEvent(task, TaskAuditAction.TASK_FAILED, beforeFailure, task.getStatus(), "Processing failed permanently as the retry limit exceeded", "SYSTEM");
            } else {
                task.markAsRetryPending();
                log.info("Task {} moved to RETRY_PENDING. retryCount = {}", task.getId(), task.getRetryCount());
                taskAuditService.logTaskEvent(task, TaskAuditAction.RETRY_TRIGGERED, beforeFailure, task.getStatus(), "Processing failed, triggering retry: " + task.getRetryCount(), "SYSTEM");
            }
        }
        taskRepository.save(task);
        log.info("Processing finished. taskId = {}, finalStatus = {}", task.getId(), task.getStatus());
    }

    @Override
    public RetryTaskResponseDto reProcessFailedTask(Long taskId, MultipartFile file) {
        log.info("Starting task reprocessing. taskId = {}", taskId);
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        log.debug("Loaded task with status {}. taskId = {}, retryCount = {}", task.getStatus(), task.getId(), task.getRetryCount());
        if (task.getStatus() != TaskStatus.PERMANENT_FAILURE) {
            log.error("Task {} is in invalid state {}. Valid state is PERMANENT_FAILURE", task.getId(), task.getStatus());
            throw new InvalidTaskStateException("Only permanently failed tasks can be re-tried");
        }
        TaskStatus beforeFileUpload = task.getStatus();
        log.debug("Validating replacement file. file = {}", file.getName());
        taskFileValidator.validate(file, task.getTaskType());
        log.debug("File validation successful. file = {}", file.getOriginalFilename());

        log.info("Uploading replacement file for task {}", task.getId());
        FileUploadResult fileUploadResult = fileStoragePort.storeFile(file);
        log.info("Replacement file stored successfully. storedFilename = {}, size = {} bytes", fileUploadResult.storedFilename(), fileUploadResult.fileSize());
        task.setOriginalFileName(fileUploadResult.originalFileName());
        task.setStoredFileName(fileUploadResult.storedFilename());
        task.setFilePath(fileUploadResult.filePath());
        task.setFileSize(fileUploadResult.fileSize());
        task.markAsFileUploaded();

        taskAuditService.logTaskEvent(task, TaskAuditAction.FILE_UPLOADED, beforeFileUpload, task.getStatus(), "File uploaded", securityUtil.getUsername());
        TaskStatus beforeQueue = task.getStatus();
        task.markAsQueued();
        Task savedTask = taskRepository.save(task);
        taskAuditService.logTaskEvent(savedTask, TaskAuditAction.TASK_RE_QUEUED, beforeQueue, savedTask.getStatus(), "Task re-queued to process failed task", securityUtil.getUsername());
        log.info("Task queued successfully. taskId = {}, status = {}", savedTask.getId(), savedTask.getStatus());
        return TaskMapper.toRetryTaskResponseDto(task);
    }
}
