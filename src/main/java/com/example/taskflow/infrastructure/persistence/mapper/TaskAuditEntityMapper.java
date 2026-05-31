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
        entity.setOldStatus(taskAudit.getOldStatus());
        entity.setNewStatus(taskAudit.getNewStatus());
        entity.setMessage(taskAudit.getMessage());
        entity.setCreatedAt(taskAudit.getCreatedAt());
        entity.setPerformedBy(taskAudit.getPerformedBy());

        return entity;
    }

    public static TaskAudit toDomain(TaskAuditEntity entity) {
        TaskAudit audit = new TaskAudit(
                entity.getTaskId(),
                entity.getAction(),
                entity.getOldStatus(),
                entity.getNewStatus(),
                entity.getPerformedBy(),
                entity.getMessage()
        );
        audit.setId(entity.getId());

        return audit;
    }
}
