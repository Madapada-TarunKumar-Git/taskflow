package com.example.taskflow.application.service;

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
@Transactional
public class TaskSchedulerService {
    private final TaskRepository taskRepository;
    private final AsyncTaskProcessorService asyncTaskProcessorService;

    @Scheduled(fixedDelay = 30000) // 30 Seconds
    public void processQueuedTasks() {
        log.info("Checking for queued tasks");
        List<Task> queuedTasks = taskRepository.findByStatus(TaskStatus.QUEUED);
        if (queuedTasks.isEmpty()) {
            log.info("No tasks in queue");
            return;
        }
        log.info("Found {} tasks in queue", queuedTasks.size());

        for (Task task : queuedTasks) {
            boolean taskClaimedSuccessfully  = taskRepository.claimTaskForProcessing(task.getId());
            // if taskClaimedSuccessfully = true ( means 1 == 1 rows updated)
            if (!taskClaimedSuccessfully ) { // !false = true ; means the task already updated to process
                log.info("Task already claimed: {}", task.getId());
                continue;
            }
            asyncTaskProcessorService.processTaskAsync(task.getId());
        }
    }
}
