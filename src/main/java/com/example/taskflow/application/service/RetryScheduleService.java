package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryScheduleService {
    private final TaskRepository taskRepository;
    private final TaskAuditService taskAuditService;

    @Scheduled(fixedDelay = 60000) // 60 seconds
    public  void retryFailedTasks(){
        List<Task> retryTasks = taskRepository.findByStatus(TaskStatus.RETRY_PENDING);

        if (retryTasks.isEmpty()) return;

        log.info("Found {} retry pending tasks",retryTasks.size());
        for (Task task : retryTasks) {
            TaskStatus beforeQueued = task.getStatus();
            task.markAsQueued();
            taskAuditService.logTaskEvent(task, TaskAuditAction.TASK_RE_QUEUED, beforeQueued, task.getStatus(), "Task Re-Queued", "System");
            taskRepository.save(task);
            log.info("Task re-queued for retry: {}", task.getId());
        }
    }
}
