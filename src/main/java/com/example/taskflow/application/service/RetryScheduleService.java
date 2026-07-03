package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryScheduleService {
    private final TaskRepository taskRepository;
    private final RetryTaskService retryTaskService;

    @Scheduled(fixedDelay = 60000) // 60 seconds
    public void retryPendingTasks() {
        log.info("Retry scheduler started");
        List<Task> retryTasks = taskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.RETRY_PENDING);
        int reQueuedTasks = 0;
        if (retryTasks.isEmpty()) {
            log.info("No retry pending tasks found");
            return;
        }

        log.info("Found {} retry pending task(s)", retryTasks.size());

        for (Task task : retryTasks) {
            try {
                retryTaskService.retryTask(task.getId());
                reQueuedTasks++;
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.warn("Skip retry for task {} because it was modified by another transaction.", task.getId());
            }
        }
        log.info("Retry scheduler completed. Re-Queued {} task(s).", reQueuedTasks);
    }
}
