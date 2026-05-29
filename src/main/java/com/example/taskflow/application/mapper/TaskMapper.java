package com.example.taskflow.application.mapper;

import com.example.taskflow.application.dto.TaskResponseDto;
import com.example.taskflow.domain.model.Task;

public final class TaskMapper {
    private TaskMapper(){}

    public static TaskResponseDto toResponseDto (Task task){
        return new TaskResponseDto(
                task.getId(),
                task.getTaskName(),
                task.getDescription(),
                task.getTaskType(),
                task.getStatus(),
                task.getPriority(),
                task.getRetryCount(),
                task.getCreatedBy(),
                task.getCreatedAt()
        );
    }
}
