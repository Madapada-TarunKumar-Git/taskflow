package com.example.taskflow.presentation.response;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record UploadResponseDto(
        @Schema(example = "1")
        Long taskId,

        @Schema(example = "Customer Import")
        String taskName,

        @Schema(example = "customer details....")
        String description,

        @Schema(example = "CUSTOMER_IMPORT")
        TaskType taskType,

        @Schema(example = "QUEUED")
        TaskStatus status,

        @Schema(example = "HIGH")
        TaskPriority priority,

        @Schema(example = "John Doe")
        String uploadedBy,

        Instant uploadAt
) {
}
