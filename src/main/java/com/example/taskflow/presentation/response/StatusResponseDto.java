package com.example.taskflow.presentation.response;

public record StatusResponseDto(
        long totalTasks,
        long completedTasks,
        long processingTasks,
        long failedTasks,
        long retryPendingTasks,
        long queuedTasks,
        long createdTasks
) {
}
