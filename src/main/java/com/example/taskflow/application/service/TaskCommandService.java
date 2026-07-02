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

        return TaskMapper.toCreateTaskResponseDto(savedTask);
    }

    @Override
    public UploadResponseDto uploadTask(UploadTaskCommand command, MultipartFile file) {
        taskFileValidator.validate(file, command.taskType());
        FileUploadResult fileUploadResult = fileStoragePort.storeFile(file);

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


        return TaskMapper.toUploadResponseDto(savedTask);
    }

    @Override
    public void processTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new TaskNotFoundException("Task not found with id: " + taskId));

        try {
            log.info("Task processing started for: {}", taskId);
            TaskProcessingResultDto result = customerCsvProcessingService.process(task.getFilePath());
            task.updateProcessingStatistics(
                    result.totalRecords(),
                    result.successfulRecords(),
                    result.failedRecords()
            );

            TaskStatus beforeCompletion = task.getStatus();
            if (result.failedRecords() > 0) {
                task.markAsPartiallyCompleted();
                taskAuditService.logTaskEvent(task, TaskAuditAction.PROCESSING_PARTIALLY_COMPLETED, beforeCompletion, task.getStatus(), "Processing partially completed", "SYSTEM");
            } else {
                task.markAsCompleted();
                log.info("Task -{} successfully processed", task.getId());
                taskAuditService.logTaskEvent(task, TaskAuditAction.TASK_COMPLETED, beforeCompletion, task.getStatus(), "Processing completed", "SYSTEM");
            }
        } catch (Exception ex) {
            log.info("Task processing failed for: {}", task.getId(), ex);
            TaskStatus beforeFailure = task.getStatus();
            task.incrementRetryCount();
            if (task.hasReachedMaxRetryLimit()) {
                log.info("Retry exceeded maximum limit for task: {}", task.getId(), ex);
                task.markAsPermanentFailure(ex.getMessage());
                taskAuditService.logTaskEvent(task, TaskAuditAction.TASK_FAILED, beforeFailure, task.getStatus(), "Processing failed permanently as the retry limit exceeded", "SYSTEM");
            } else {
                task.markAsRetryPending();
                taskAuditService.logTaskEvent(task, TaskAuditAction.RETRY_TRIGGERED, beforeFailure, task.getStatus(), "Processing failed, triggering retry: " + task.getRetryCount(), "SYSTEM");
            }
        }
        taskRepository.save(task);
    }

    @Override
    public RetryTaskResponseDto reProcessFailedTask(Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        if (task.getStatus() != TaskStatus.PERMANENT_FAILURE) {
            throw new InvalidTaskStateException("Only permanently failed tasks can be re-tried");
        }
        TaskStatus beforeFileUpload = task.getStatus();
        taskFileValidator.validate(file, task.getTaskType());
        FileUploadResult fileUploadResult = fileStoragePort.storeFile(file);

        task.setOriginalFileName(fileUploadResult.originalFileName());
        task.setStoredFileName(fileUploadResult.storedFilename());
        task.setFilePath(fileUploadResult.filePath());
        task.setFileSize(fileUploadResult.fileSize());
        task.markAsFileUploaded();

        taskAuditService.logTaskEvent(task, TaskAuditAction.FILE_UPLOADED, beforeFileUpload, task.getStatus(), "File uploaded", securityUtil.getUsername());
        log.info("Before retry status: {}", beforeFileUpload);
        TaskStatus beforeQueue = task.getStatus();
        task.markAsQueued();
        Task savedTask = taskRepository.save(task);
        taskAuditService.logTaskEvent(savedTask, TaskAuditAction.TASK_RE_QUEUED, beforeQueue, savedTask.getStatus(), "Task re-queued to process failed task", securityUtil.getUsername());
        return TaskMapper.toRetryTaskResponseDto(task);
    }
}
