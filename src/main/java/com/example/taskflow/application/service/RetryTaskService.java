package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryTaskService {
    private final TaskRepository taskRepository;
    private final TaskAuditService taskAuditService;

    @Transactional
    public void retryTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        TaskStatus beforeQueued = task.getStatus();
        task.markAsQueued();
        Task savedTask = taskRepository.save(task);
        taskAuditService.logTaskEvent(savedTask, TaskAuditAction.TASK_RE_QUEUED, beforeQueued, savedTask.getStatus(), "Task Re-Queued", "SYSTEM");
        log.info("Task {} successfully moved from {} to {}", task.getId(), beforeQueued, savedTask.getStatus());

    }
}
