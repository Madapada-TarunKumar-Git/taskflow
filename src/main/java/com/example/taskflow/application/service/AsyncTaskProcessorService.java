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
        long start = System.currentTimeMillis();
        log.debug("Running on thread = {}", Thread.currentThread().getName());
        log.info("Async processing started for taskId = {}", taskId);
        try {
            taskCommandService.processTask(taskId);
            long duration = System.currentTimeMillis() - start;
            log.info("Async processing completed for taskId = {} in {} ms", taskId, duration);
        } catch (Exception ex) {
            log.error("Unexpected error occurred while processing taskI = {}", taskId, ex);
        } finally {
            log.debug("Async processor finished for taskId = {}", taskId);
        }
    }
}
