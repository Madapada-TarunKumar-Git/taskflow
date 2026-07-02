package com.example.taskflow.infrastructure.persistence.mapper;

import com.example.taskflow.domain.model.TaskAudit;
import com.example.taskflow.infrastructure.persistence.entity.TaskAuditEntity;

public final class TaskAuditEntityMapper {
    private TaskAuditEntityMapper() {
    }

    public static TaskAuditEntity toEntity(TaskAudit taskAudit) {
        TaskAuditEntity entity = new TaskAuditEntity();

        entity.setId(taskAudit.getId());
        entity.setTaskId(taskAudit.getTaskId());
        entity.setAction(taskAudit.getAction());
        entity.setFromStatus(taskAudit.getFromStatus());
        entity.setToStatus(taskAudit.getToStatus());
        entity.setMessage(taskAudit.getMessage());
        entity.setCreatedAt(taskAudit.getCreatedAt());
        entity.setPerformedBy(taskAudit.getPerformedBy());

        return entity;
    }

    public static TaskAudit toDomain(TaskAuditEntity entity) {
        TaskAudit audit = new TaskAudit(
                entity.getTaskId(),
                entity.getAction(),
                entity.getFromStatus(),
                entity.getToStatus(),
                entity.getMessage(),
                entity.getPerformedBy()
        );
        audit.setId(entity.getId());

        return audit;
    }
}
