package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.model.TaskAudit;
import com.example.taskflow.domain.repository.TaskAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAuditService {
    private final TaskAuditRepository taskAuditRepository;

    public void logTaskEvent(
            Task task,
            TaskAuditAction action,
            TaskStatus oldStatus,
            TaskStatus newStatus,
            String message,
            String performedBy
    ){
        TaskAudit audit = new TaskAudit(
                task.getId(),
                action,
                oldStatus,
                newStatus,
                message,
                performedBy
        );
        taskAuditRepository.save(audit);
    }
}
