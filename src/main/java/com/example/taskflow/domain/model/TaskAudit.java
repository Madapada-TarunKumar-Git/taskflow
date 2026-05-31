package com.example.taskflow.domain.model;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
public class TaskAudit {
    @Setter
    private Long id;
    private Long taskId;
    private TaskAuditAction action;
    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private String message;
    private String performedBy;
    private Instant createdAt;

    public TaskAudit(
            Long taskId,
            TaskAuditAction action,
            TaskStatus oldStatus,
            TaskStatus newStatus,
            String message,
            String performedBy
    ) {
        validate(taskId,action,performedBy);

        this.taskId = taskId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.message = message;
        this.performedBy = performedBy;
        this.createdAt = Instant.now();
    }

    private void validate(
            Long taskId,
            TaskAuditAction action,
            String performedBy
    ) {

        if (taskId == null) {
            throw new IllegalArgumentException("Task ID is required");
        }

        if (action == null) {
            throw new IllegalArgumentException("Action is required");
        }

        if (performedBy == null || performedBy.isBlank()) {
            throw new IllegalArgumentException("PerformedBy is required");
        }
    }
}
