package com.example.taskflow.domain.repository;

import com.example.taskflow.domain.model.TaskAudit;

import java.util.List;

public interface TaskAuditRepository {
    TaskAudit save(TaskAudit taskAudit);
    List<TaskAudit> findByTaskId(Long taskId);
}
