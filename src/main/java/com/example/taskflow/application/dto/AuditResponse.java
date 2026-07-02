package com.example.taskflow.application.dto;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;

import java.time.Instant;

public record AuditResponse(
        Long auditId,
        TaskAuditAction action,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        String message,
        String performedBy,
        Instant createdAt
) {
}
