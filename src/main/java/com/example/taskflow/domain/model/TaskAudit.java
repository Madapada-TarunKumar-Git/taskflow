package com.example.taskflow.domain.model;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
public class TaskAudit {
    @Setter
    private Long id;
    private Long taskId;
    private TaskAuditAction action;
    private TaskStatus fromStatus;
    private TaskStatus toStatus;
    private String message;
    private String performedBy;
    private Instant createdAt;

    public TaskAudit(
            Long taskId,
            TaskAuditAction action,
            TaskStatus fromStatus,
            TaskStatus toStatus,
            String message,
            String performedBy
    ) {
        validate(taskId,action,performedBy);

        this.taskId = taskId;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
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
