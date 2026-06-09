package com.example.taskflow.application.usecase;

import com.example.taskflow.application.dto.StatusResponse;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.shared.response.PageResponse;

public interface TaskQueryUseCase {
    // For read operation services
    TaskResponseDto getTaskById(Long taskId);
    PageResponse<TaskResponseDto> getTasks(int page, int size, String sortBy, String direction, TaskStatus status);
    StatusResponse getTaskStatistics();

}
