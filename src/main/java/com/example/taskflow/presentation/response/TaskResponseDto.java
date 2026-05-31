package com.example.taskflow.presentation.response;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;

import java.time.Instant;

public record TaskResponseDto(
        Long id,
        String taskName,
        String description,
        TaskType taskType,
        TaskStatus status,
        TaskPriority priority,
        int retryCount,
        String createdBy,
        Instant createdAt
) {
}
