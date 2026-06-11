package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.dto.AuditResponse;
import com.example.taskflow.application.dto.StatusResponse;
import com.example.taskflow.application.usecase.TaskAuditQueryUseCase;
import com.example.taskflow.presentation.request.UploadTaskRequest;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.application.usecase.TaskQueryUseCase;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.presentation.mapper.TaskRequestMapper;
import com.example.taskflow.presentation.request.CreateTaskRequest;
import com.example.taskflow.shared.response.ApiResponse;
import com.example.taskflow.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Task Management", description = "APIs for task processing workflow")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskCommandUseCase taskCommandUseCase;
    private final TaskQueryUseCase taskQueryUseCase;
    private final TaskAuditQueryUseCase taskAuditQueryUseCase;

    @Operation(summary = "Create tasks", description = "Task creation with type, priority")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created successfully")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponseDto response = taskCommandUseCase.createTask(TaskRequestMapper.toCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    @Operation(summary = "Get tasks by Id", description = "Retrieve detailed information about a task")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task retrieved successfully")
    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTaskById(@PathVariable Long taskId) {
        TaskResponseDto response = taskQueryUseCase.getTaskById(taskId);

        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response));
    }

    @Operation(summary = "Get all tasks", description = "Retrieve list of task details based on status")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskResponseDto>>> getTasks(
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
                ApiResponse.success("Tasks retrieved successfully", pageResponse));
    }

    @Operation(summary = "Upload task", description = "Create task and upload CSV file for async processing")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task uploaded successfully")
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TaskResponseDto>> uploadFile(
            @Valid @ModelAttribute UploadTaskRequest request) {
        UploadTaskCommand command = TaskRequestMapper.toUploadCommand(request);

        TaskResponseDto response = taskCommandUseCase.uploadTask(command, request.file());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task uploaded successfully", response));
    }

    @Operation(summary = "Task Statistics", description = "retrieve the task statistics")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics of tasks retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<StatusResponse>> getStatistics() {
        StatusResponse response = taskQueryUseCase.getTaskStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics of tasks retrieved successfully", response));
    }

    @Operation(summary = "Task audits", description = "Retrieve task flow by id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task audit history retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{taskId}/audits")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getTaskAudits(@PathVariable Long taskId) {
        List<AuditResponse> response = taskAuditQueryUseCase.getTaskAudits(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task audit history retrieved successfully", response));
    }

    @Operation(summary = "Retry task", description = "Retry the permanently failed task with id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task re-tried successfully and moved to queued")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{taskId}/retry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TaskResponseDto>> retryTask(
            @PathVariable Long taskId,
            @RequestPart("file") MultipartFile file
            ) {
        TaskResponseDto response = taskCommandUseCase.retryTask(taskId,file);
        return ResponseEntity.ok(ApiResponse.success("Task re-tried successfully", response));
    }
}
