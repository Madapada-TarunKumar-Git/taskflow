package com.example.taskflow.application.dto;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;

import java.time.LocalDateTime;

public record TaskResponseDto(
        Long id,
        String taskName,
        String description,
        TaskType taskType,
        TaskStatus status,
        TaskPriority priority,
        int retryCount,
        String createdBy,
        LocalDateTime createdAt
) {
}
