package com.example.taskflow.domain.model;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.exception.InvalidTaskStateException;
import com.example.taskflow.domain.exception.MaxRetryLimitExceededException;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Task {
    private static final int MAX_RETRY_COUNT = 3;
    private Long id;
    private String taskName;
    private String description;
    private TaskType taskType;
    @Setter
    private TaskStatus status;
    private TaskPriority priority;
    private int retryCount;
    private String createdBy;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    @Setter
    private String originalFileName;
    @Setter
    private String storedFileName;
    @Setter
    private String filePath;
    @Setter
    private Long fileSize;

    private Integer totalRecords;
    private Integer successRecords;
    private Integer failedRecords;

    private Instant processingStartedAt;
    private Instant processingCompletedAt;


    public Task(
            String taskName,
            String description,
            TaskType taskType,
            TaskPriority priority,
            String createdBy
    ) {
        validateTaskName(taskName);
        this.taskName = taskName;
        this.description = description;
        this.taskType = Objects.requireNonNull(taskType);
        this.priority = Objects.requireNonNull(priority);
        this.createdBy = Objects.requireNonNull(createdBy);

        this.status = TaskStatus.CREATED;
        this.retryCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        this.totalRecords = 0;
        this.successRecords = 0;
        this.failedRecords = 0;
    }

    public static Task restore(Long id, String taskName, String description,
                               TaskType taskType, TaskStatus status,
                               TaskPriority priority, int retryCount,
                               String createdBy, Instant createdAt,
                               Instant updatedAt, String failureReason,
                               String originalFileName, String storedFileName,
                               String filePath, Long fileSize,
                               Integer totalRecords, Integer successRecords,
                               Integer failedRecords, Instant processingStartedAt,
                               Instant processingCompletedAt) {
        Task task = new Task(
                taskName,
                description,
                taskType,
                priority,
                createdBy
        );

        task.id = id;
        task.status = status;
        task.retryCount = retryCount;
        task.createdAt = createdAt;
        task.updatedAt = updatedAt;
        task.failureReason = failureReason;
        task.originalFileName = originalFileName;
        task.storedFileName = storedFileName;
        task.filePath = filePath;
        task.fileSize = fileSize;
        task.totalRecords = totalRecords;
        task.successRecords = successRecords;
        task.failedRecords = failedRecords;
        task.processingStartedAt = processingStartedAt;
        task.processingCompletedAt = processingCompletedAt;

        return task;
    }

    public void markAsValidating() {
        validateCurrentStatus(TaskStatus.FILE_UPLOADED);

        this.status = TaskStatus.VALIDATING;
        updateTimestamp();
    }

    public void markAsProcessing() {
        validateCurrentStatus(TaskStatus.QUEUED);
        this.status = TaskStatus.PROCESSING;
        this.processingStartedAt = Instant.now();
        updateTimestamp();
    }

    public void markAsPartiallyCompleted() {
        validateCurrentStatus(TaskStatus.PROCESSING);
        this.status = TaskStatus.PARTIALLY_COMPLETED;
        this.processingCompletedAt = Instant.now();
        updateTimestamp();
    }

    public void markAsCompleted() {
        validateCurrentStatus(TaskStatus.PROCESSING);
        this.status = TaskStatus.COMPLETED;
        this.processingCompletedAt = Instant.now();
        updateTimestamp();
    }

    public void markAsFailed(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Failure reason cannot be empty");
        }

        this.status = TaskStatus.FAILED;
        this.failureReason = reason;
        updateTimestamp();
    }

    public void markAsFileUploaded() {
        validateCurrentStatus(TaskStatus.CREATED);
        this.status = TaskStatus.FILE_UPLOADED;
        updateTimestamp();
    }

    public void markAsQueued() {
//        validateCurrentStatus(TaskStatus.FILE_UPLOADED);
        if (this.status == TaskStatus.FILE_UPLOADED
                || this.status == TaskStatus.RETRY_PENDING) {
            this.status = TaskStatus.QUEUED;
            updateTimestamp();
            return;
        }
        throw new InvalidTaskStateException("Task cannot be queued from status: " + this.status);
    }

    public void markAsRetryPending() {
        validateCurrentStatus(TaskStatus.PROCESSING);
        this.status = TaskStatus.RETRY_PENDING;
        updateTimestamp();
    }

    public void markAsPermanentFailure(String reason) {
        this.status = TaskStatus.PERMANENT_FAILURE;
        this.failureReason = reason;
        updateTimestamp();
    }

    public void incrementRetryCount() {
//        if (this.retryCount >= MAX_RETRY_COUNT) {
//            throw new MaxRetryLimitExceededException("Maximum retry limit exceeded");
//        }

        this.retryCount++;
        updateTimestamp();
    }

//    public void validateCurrentStatus(TaskStatus expectedStatus) {
//        if (this.status != expectedStatus) {
//            throw new InvalidTaskStateException("Invalid task state transition from"
//                    + this.status
//                    + "to"
//                    + expectedStatus);
//        }
//
//    }

    public boolean hasReachedMaxRetryLimit() {
        return this.retryCount >= Task.MAX_RETRY_COUNT;
    }

    public void validateCurrentStatus(TaskStatus... allowedStatuses) {
        for (TaskStatus allowedStatus : allowedStatuses) {
            if (this.status == allowedStatus) return;
        }

        throw new InvalidTaskStateException("Invalid task state transition from "
                + this.status
        );
    }

    private void validateTaskName(String taskName) {
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("Task name cannot be empty");
        }
    }

    public void updateProcessingStatistics(int totalRecords, int successRecords, int failedRecords) {
        this.totalRecords = totalRecords;
        this.successRecords = successRecords;
        this.failedRecords = failedRecords;
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = Instant.now();
    }
}
