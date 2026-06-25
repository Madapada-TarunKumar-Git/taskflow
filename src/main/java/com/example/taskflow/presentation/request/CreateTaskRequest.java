package com.example.taskflow.presentation.request;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Task name is required")
        @Size(max = 100, message = "Task name cannot exceed 100 characters")
        String taskName,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotNull(message = "Task type is required")
        TaskType taskType,

        @NotNull(message = "Task priority is required")
        TaskPriority priority
) {}