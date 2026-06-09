package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryScheduleService {
    private final TaskRepository taskRepository;
    private final TaskAuditService taskAuditService;

    @Scheduled(fixedDelay = 60000) // 60 seconds
    @Transactional
    public void retryFailedTasks() {
        List<Task> retryTasks = taskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.RETRY_PENDING);

        if (retryTasks.isEmpty()) return;

        log.info("Found {} retry pending tasks", retryTasks.size());

        for (Task task : retryTasks) {
            try {
                TaskStatus beforeQueued = task.getStatus();
                task.markAsQueued();
                Task savedTask = taskRepository.save(task);
                taskAuditService.logTaskEvent(savedTask, TaskAuditAction.TASK_RE_QUEUED, beforeQueued, savedTask.getStatus(), "Task Re-Queued", savedTask.getCreatedBy());
                log.info("Task re-queued for retry: {}", task.getId());
            } catch(ObjectOptimisticLockingFailureException ex){
            log.warn("Task updated by another transaction: {}", task.getId());
        }
            }
    }
}
