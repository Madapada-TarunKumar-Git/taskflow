package com.example.taskflow.application.mapper;

import com.example.taskflow.application.dto.AuditResponse;
import com.example.taskflow.domain.model.TaskAudit;

public final class TaskAuditMapper {
    public static AuditResponse toAuditResponse(TaskAudit taskAudit) {
        return new AuditResponse(
                taskAudit.getId(),
                taskAudit.getAction(),
                taskAudit.getFromStatus(),
                taskAudit.getToStatus(),
                taskAudit.getMessage(),
                taskAudit.getPerformedBy(),
                taskAudit.getCreatedAt()
        );
    }
}
