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
import com.example.taskflow.shared.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "TaskCommandController", description = "Create/edit task details")
public class TaskCommandController {
    private final TaskCommandUseCase taskCommandUseCase;
    private final SecurityUtil securityUtil;

    @Operation(summary = "Create tasks", description = "Task creation with type, priority")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @PostMapping
    public ResponseEntity<APIResponse<CreateTaskResponseDto>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Create task request received. user = {}, taskName = {}, taskType = {}", securityUtil.getUsername(), request.taskName(), request.taskType());
        CreateTaskResponseDto response = taskCommandUseCase.createTask(TaskRequestMapper.toCreateCommand(request));
        log.info("Task created successfully. taskId = {}, user = {} ", response.taskId(), securityUtil.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Task created successfully", response));
    }

    @Operation(summary = "Upload task", description = "Create task and upload CSV file for async processing")
    @ApiResponse(responseCode = "201", description = "Task uploaded successfully")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<UploadResponseDto>> uploadFile(
            @Valid @ModelAttribute UploadTaskRequest request) {
        log.info("Upload task request received. user = {}, taskName = {}, taskType = {}, filename = {}, fileSize = {}",
                securityUtil.getUsername(), request.taskName(), request.taskType(), request.file().getName(), request.file().getSize());
        UploadTaskCommand command = TaskRequestMapper.toUploadCommand(request);

        UploadResponseDto response = taskCommandUseCase.uploadTask(command, request.file());

        log.info("Task uploaded successfully. taskId = {}, user = {} ", response.taskId(), securityUtil.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Task uploaded successfully", response));
    }

    @Operation(summary = "Retry task", description = "Retry the permanently failed task with id")
    @ApiResponse(responseCode = "200", description = "Task reprocess initiated successfully and moved to queued")
    @PutMapping(value = "/{taskId}/retry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<RetryTaskResponseDto>> retryTask(
            @PathVariable Long taskId,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("Reprocess request received. user = {}, taskId = {}, filename = {}, fileSize = {}",
                securityUtil.getUsername(), taskId, file.getName(), file.getSize());
        RetryTaskResponseDto response = taskCommandUseCase.reProcessFailedTask(taskId, file);
        log.info("Task reprocess initiated successfully. taskId = {}", taskId);
        return ResponseEntity.ok(APIResponse.success("Task reprocess initiated successfully", response));
    }
}
