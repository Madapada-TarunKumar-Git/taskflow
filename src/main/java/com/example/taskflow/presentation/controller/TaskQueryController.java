package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.dto.StatusResponse;
import com.example.taskflow.application.usecase.TaskAuditQueryUseCase;
import com.example.taskflow.application.usecase.TaskQueryUseCase;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.shared.response.APIResponse;
import com.example.taskflow.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "TaskQueryController", description = "Retrieve task details")
public class TaskQueryController {
    private final TaskQueryUseCase taskQueryUseCase;
    private final TaskAuditQueryUseCase taskAuditQueryUseCase;

    @Operation(summary = "Get tasks by Id", description = "Retrieve detailed information about a task")
    @ApiResponse(responseCode = "200", description = "Task retrieved successfully")
    @GetMapping("/{taskId}")
    public ResponseEntity<APIResponse<TaskResponseDto>> getTaskById(@PathVariable Long taskId) {
        TaskResponseDto response = taskQueryUseCase.getTaskById(taskId);

        return ResponseEntity.ok(APIResponse.success("Task retrieved successfully", response));
    }

    @Operation(summary = "Get all tasks", description = "Retrieve list of task details based on status")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<TaskResponseDto>>> getTasks(
            @Min(0) @RequestParam(name = "page", defaultValue = "0")
            int page,

            @Min(10) @Max(100)
            @RequestParam(name = "size", defaultValue = "10")
            int size,

            @RequestParam(name = "sortBy", defaultValue = "createdAt")
            String sortBy,

            @RequestParam(name = "direction", defaultValue = "desc")
            String direction,

            @RequestParam(name = "status", required = false)
            TaskStatus status
    ) {
        PageResponse<TaskResponseDto> pageResponse = taskQueryUseCase
                .getTasks(page, size, sortBy, direction, status);
        return ResponseEntity.ok(
                APIResponse.success("Tasks retrieved successfully", pageResponse));
    }


    @Operation(summary = "Task Statistics", description = "retrieve the task statistics")
    @ApiResponse(responseCode = "200", description = "Statistics of tasks retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<APIResponse<StatusResponse>> getStatistics() {
        StatusResponse response = taskQueryUseCase.getTaskStatistics();
        return ResponseEntity.ok(APIResponse.success("Statistics of tasks retrieved successfully", response));
    }
}
