package com.example.taskflow.domain.model;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.exception.InvalidTaskStateException;
import com.example.taskflow.domain.exception.MaxRetryLimitExceedException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Task {
    private static final int MAX_RETRY_COUNT = 3;
    private Long id;
    private String taskName;
    private String description;
    private TaskType taskType;
    private TaskStatus status;
    private TaskPriority priority;
    private int retryCount;
    private String createdBy;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Task restore(Long id, String taskName, String desc, TaskType taskType,
                               TaskStatus status, TaskPriority priority, int retryCount,
                               String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt, String failureReason) {
        Task task = new Task(
                taskName,
                desc,
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

        return task;
    }

    public void markAsValidating() {
        validateCurrentStatus(TaskStatus.CREATED);

        this.status = TaskStatus.VALIDATING;
        updateTimestamp();
    }

    public void markAsProcessing() {
        validateCurrentStatus(TaskStatus.VALIDATING);
        ;

        this.status = TaskStatus.PROCESSING;
        updateTimestamp();
    }

    public void markAsCompleted() {
        validateCurrentStatus(TaskStatus.PROCESSING);

        this.status = TaskStatus.COMPLETED;
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

    public void incrementRetryCount() {
        if (this.retryCount >= MAX_RETRY_COUNT) {
            throw new MaxRetryLimitExceedException("Maximum retry limit exceeded");
        }

        this.retryCount++;
        updateTimestamp();
    }

    public void validateCurrentStatus(TaskStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new InvalidTaskStateException("Invalid task state transition from"
                    + this.status
                    + "to"
                    + expectedStatus);
        }

    }

    private void validateTaskName(String taskName) {
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("Task name cannot be empty");
        }
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
