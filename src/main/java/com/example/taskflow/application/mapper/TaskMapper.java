package com.example.taskflow.application.mapper;

import com.example.taskflow.application.dto.TaskResponseDto;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.shared.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public final class TaskMapper {
    private TaskMapper() {
    }

    public static TaskResponseDto toResponseDto(Task task) {
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

    public static PageResponse<TaskResponseDto> toPageResponse(
            List<TaskResponseDto> content, Page<Task> taskPage) {
        return new PageResponse<>(
                content,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }
}
