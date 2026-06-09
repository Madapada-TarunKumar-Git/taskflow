package com.example.taskflow.application.usecase;

import com.example.taskflow.application.dto.AuditResponse;

import java.util.List;

public interface TaskAuditQueryUseCase {
    List<AuditResponse> getTaskAudits(Long taskId);
}
