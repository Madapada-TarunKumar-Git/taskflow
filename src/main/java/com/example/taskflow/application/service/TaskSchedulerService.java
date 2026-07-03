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
        log.info("Task Scheduler started");
        List<Task> queuedTasks = taskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.QUEUED);
        int submittedTasks = 0;
        if (queuedTasks.isEmpty()) {
            log.info("No tasks in queue");
            log.info("Task Scheduler cycle finished");
            return;
        }
        log.info("Found {} tasks in queue", queuedTasks.size());

        for (Task task : queuedTasks) {
            boolean taskClaimedSuccessfully = taskRepository.claimTaskForProcessing(task.getId());
            // if taskClaimedSuccessfully = true ( means 1 == 1 rows updated)
            if (!taskClaimedSuccessfully) { // !false = true ; means the task already updated to process
                log.debug("Skipping task {} because it has already been claimed", task.getId());
                continue;
            }
            log.info("Task {} claimed for processing", task.getId());
            Task claimedTask = taskRepository.findById(task.getId()).orElseThrow();
            taskAuditService.logTaskEvent(claimedTask, TaskAuditAction.PROCESSING_STARTED, TaskStatus.QUEUED, claimedTask.getStatus(), "Claimed for processing", "SYSTEM");
            log.info("Task {} submitting for asynchronous", task.getId());
            asyncTaskProcessorService.processTaskAsync(task.getId());
            submittedTasks++;
        }
        log.info("Scheduler cycle completed. submittedTasks = {}", submittedTasks);
    }
}
