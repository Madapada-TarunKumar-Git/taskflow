package com.example.taskflow.presentation.request;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadTaskRequest(
        @NotBlank(message = "Task name is required")
        String taskName,

        String description,

        @NotNull(message = "Task type is required")
        TaskType taskType,

        @NotNull(message = "Priority is required")
        TaskPriority priority,

        MultipartFile file
) {
}
