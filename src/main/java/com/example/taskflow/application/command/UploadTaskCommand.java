package com.example.taskflow.application.command;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskType;

public record UploadTaskCommand(
        String taskName,
        String description,
        TaskType taskType,
        TaskPriority priority
) {
}
