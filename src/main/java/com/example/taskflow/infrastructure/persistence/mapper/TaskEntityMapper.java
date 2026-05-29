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

        return entity;
    }

    public static Task toDomain(TaskEntity entity){
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
                entity.getFailureReason()
        );
    }
}
