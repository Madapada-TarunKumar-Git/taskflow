package com.example.taskflow.application.service;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.dto.TaskProcessingResultDto;
import com.example.taskflow.presentation.response.TaskProcessingResponseDto;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.application.mapper.TaskMapper;
import com.example.taskflow.application.port.FileStoragePort;
import com.example.taskflow.application.port.FileUploadResult;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.application.validation.TaskFileValidator;
import com.example.taskflow.domain.exception.TaskNotFoundException;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
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

    @Override
    public TaskResponseDto createTask(CreateTaskCommand command) {
        Task task = new Task(
                command.taskName(),
                command.description(),
                command.taskType(),
                command.priority(),
                command.createdBY()
        );

        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponseDto(savedTask);
    }

    @Override
    public TaskResponseDto uploadTask(UploadTaskCommand command, MultipartFile file) {
        taskFileValidator.validate(file, command.taskType());
        FileUploadResult fileUploadResult = fileStoragePort.storeFile(file);

        Task task = new Task(
                command.taskName(),
                command.description(),
                command.taskType(),
                command.priority(),
                command.createdBy()
        );

        task.setOriginalFileName(fileUploadResult.originalFileName());
        task.setStoredFileName(fileUploadResult.storedFilename());
        task.setFilePath(fileUploadResult.filePath());
        task.setFileSize(fileUploadResult.fileSize());
        task.markAsFileUploaded();
        task.markAsQueued();
        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponseDto(savedTask);
    }

    @Override
    public void processTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new TaskNotFoundException("Task not found with id: " + taskId));
        try {
            TaskProcessingResultDto result = customerCsvProcessingService.process(task.getFilePath());
            task.updateProcessingStatistics(
                    result.totalRecords(),
                    result.successfulRecords(),
                    result.failedRecords()
            );

            if (result.failedRecords() > 0) {
                task.markAsPartiallyCompleted();
            } else {
                task.markAsCompleted();
            }
        } catch (Exception ex) {
            log.info("Task processing failed for: {}", taskId, ex);
            task.incrementRetryCount();
            if (task.hasReachedMaxRetryLimit()) {
                log.info("Retry exceeded maximum limit for task: {}", taskId, ex);
                task.markAsPermanentFailure(ex.getMessage());
            } else {
                task.markAsRetryPending();
            }
        }
        taskRepository.save(task);
    }
}
