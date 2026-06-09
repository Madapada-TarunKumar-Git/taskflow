package com.example.taskflow.application.mapper;

import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.shared.response.PageResponse;
import org.springframework.data.domain.Page;

import java.time.Duration;
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
                task.getCreatedAt(),
                task.getProcessingStartedAt(),
                task.getProcessingCompletedAt(),
                Duration.between(
                        task.getProcessingStartedAt(),
                        task.getProcessingCompletedAt()
                ).toMillis()
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

//    public static TaskProcessingResponseDto toTaskProcessingResponseDto(Task task, TaskProcessingResultDto result) {
//        return new TaskProcessingResponseDto(
//                task.getId(),
//                task.getStatus(),
//                result.totalRecords(),
//                result.successfulRecords(),
//                result.failedRecords(),
//                result.errors()
//        );
//    }
}
