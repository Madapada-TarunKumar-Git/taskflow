package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerService {
    private final TaskRepository taskRepository;
    private final AsyncTaskProcessorService asyncTaskProcessorService;
    private final TaskAuditService taskAuditService;

    @Scheduled(fixedDelayString = "${task.scheduler.dealy:60000}")
    public void processQueuedTasks() {
        log.info("Checking for queued tasks");
        List<Task> queuedTasks = taskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.QUEUED);
        if (queuedTasks.isEmpty()) {
            log.info("No tasks in queue");
            return;
        }
        log.info("Found {} tasks in queue", queuedTasks.size());

        for (Task task : queuedTasks) {
            boolean taskClaimedSuccessfully = taskRepository.claimTaskForProcessing(task.getId());
            // if taskClaimedSuccessfully = true ( means 1 == 1 rows updated)
            if (!taskClaimedSuccessfully) { // !false = true ; means the task already updated to process
                log.info("Task already claimed: {}", task.getId());
                continue;
            }
            Task claimedTask = taskRepository.findById(task.getId()).orElseThrow();
            taskAuditService.logTaskEvent(claimedTask, TaskAuditAction.PROCESSING_STARTED, TaskStatus.QUEUED, claimedTask.getStatus(), "Claimed for processing", "SYSTEM");
            asyncTaskProcessorService.processTaskAsync(task.getId());
        }
    }
}
