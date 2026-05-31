package com.example.taskflow.application.dto;

import java.util.List;

public record TaskProcessingResultDto(
        int totalRecords,
        int successfulRecords,
        int failedRecords,
        List<String> errors
) {
}
