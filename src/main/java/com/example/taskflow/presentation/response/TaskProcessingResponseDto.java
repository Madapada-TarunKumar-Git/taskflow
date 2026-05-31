package com.example.taskflow.presentation.response;

import com.example.taskflow.domain.enums.TaskStatus;

import java.util.List;

public record TaskProcessingResponseDto(
        Long taskId,
        TaskStatus status,
        int totalRecords,
        int successRecords,
        int failedRecords,
        List<String> errors
) {
}
