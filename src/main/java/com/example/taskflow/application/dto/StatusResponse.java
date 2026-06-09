package com.example.taskflow.application.dto;

public record StatusResponse(
        long totalTasks,
        long completedTasks,
        long processingTasks,
        long failedTasks,
        long retryPendingTasks,
        long queuedTasks,
        long createdTasks
) {
}
