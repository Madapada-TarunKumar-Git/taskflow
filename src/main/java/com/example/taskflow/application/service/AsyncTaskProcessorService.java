package com.example.taskflow.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskProcessorService {
    private final TaskCommandService taskCommandService;

    @Async("taskProcessingExecutor")
    @Transactional
    public void processTaskAsync(Long taskId) {
        log.info("Starting async processing for task: {}", taskId);
        try {
            taskCommandService.processTask(taskId);
            log.info("Async processed for task: {}", taskId);
        } catch (Exception ex) {
            log.error("Task processing failure for task: {}", taskId, ex);
        }

    }
}
