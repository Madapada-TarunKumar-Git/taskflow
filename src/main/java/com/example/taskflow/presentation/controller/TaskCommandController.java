package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.presentation.mapper.TaskRequestMapper;
import com.example.taskflow.presentation.request.CreateTaskRequest;
import com.example.taskflow.presentation.request.UploadTaskRequest;
import com.example.taskflow.presentation.response.CreateTaskResponseDto;
import com.example.taskflow.presentation.response.RetryTaskResponseDto;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.presentation.response.UploadResponseDto;
import com.example.taskflow.shared.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "TaskCommandController", description = "Create/edit task details")
public class TaskCommandController {
    private final TaskCommandUseCase taskCommandUseCase;

    @Operation(summary = "Create tasks", description = "Task creation with type, priority")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @PostMapping
    public ResponseEntity<APIResponse<CreateTaskResponseDto>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        CreateTaskResponseDto response = taskCommandUseCase.createTask(TaskRequestMapper.toCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Task created successfully", response));
    }

    @Operation(summary = "Upload task", description = "Create task and upload CSV file for async processing")
    @ApiResponse(responseCode = "201", description = "Task uploaded successfully")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<UploadResponseDto>> uploadFile(
            @Valid @ModelAttribute UploadTaskRequest request) {
        UploadTaskCommand command = TaskRequestMapper.toUploadCommand(request);

        UploadResponseDto response = taskCommandUseCase.uploadTask(command, request.file());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Task uploaded successfully", response));
    }

    @Operation(summary = "Retry task", description = "Retry the permanently failed task with id")
    @ApiResponse(responseCode = "200", description = "Task re-tried successfully and moved to queued")
    @PutMapping(value = "/{taskId}/retry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<RetryTaskResponseDto>> retryTask(
            @PathVariable Long taskId,
            @RequestPart("file") MultipartFile file
    ) {
        RetryTaskResponseDto response = taskCommandUseCase.reProcessFailedTask(taskId,file);
        return ResponseEntity.ok(APIResponse.success("Task re-tried successfully", response));
    }
}
