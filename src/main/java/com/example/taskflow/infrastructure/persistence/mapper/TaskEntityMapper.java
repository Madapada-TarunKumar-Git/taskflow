package com.example.taskflow.infrastructure.persistence.mapper;

import com.example.taskflow.domain.model.Task;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;

public final class TaskEntityMapper {
    private TaskEntityMapper() {
    }

    public static TaskEntity toEntity(Task task) {
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setTaskName(task.getTaskName());
        entity.setDescription(task.getDescription());
        entity.setTaskType(task.getTaskType());
        entity.setStatus(task.getStatus());
        entity.setPriority(task.getPriority());
        entity.setRetryCount(task.getRetryCount());
        entity.setCreatedBy(task.getCreatedBy());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        entity.setFailureReason(task.getFailureReason());
        entity.setOriginalFileName(task.getOriginalFileName());
        entity.setStoredFileName(task.getStoredFileName());
        entity.setFilePath(task.getFilePath());
        entity.setFileSize(task.getFileSize());
        entity.setTotalRecords(task.getTotalRecords());
        entity.setSuccessRecords(task.getSuccessRecords());
        entity.setFailedRecords(task.getFailedRecords());
        entity.setProcessingStartedAt(task.getProcessingStartedAt());
        entity.setProcessingCompletedAt(task.getProcessingCompletedAt());

        return entity;
    }

    public static Task toDomain(TaskEntity entity) {
        return Task.restore(
                entity.getId(),
                entity.getTaskName(),
                entity.getDescription(),
                entity.getTaskType(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getRetryCount(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getFailureReason(),
                entity.getOriginalFileName(),
                entity.getStoredFileName(),
                entity.getFilePath(),
                entity.getFileSize(),
                entity.getTotalRecords(),
                entity.getSuccessRecords(),
                entity.getFailedRecords(),
                entity.getProcessingStartedAt(),
                entity.getProcessingCompletedAt()
        );
    }
}
