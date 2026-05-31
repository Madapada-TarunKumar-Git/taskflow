package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.presentation.response.TaskProcessingResponseDto;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.application.usecase.TaskQueryUseCase;
import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.presentation.mapper.TaskRequestMapper;
import com.example.taskflow.presentation.request.CreateTaskRequest;
import com.example.taskflow.shared.response.ApiResponse;
import com.example.taskflow.shared.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskCommandUseCase taskCommandUseCase;
    private final TaskQueryUseCase taskQueryUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTasks(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponseDto response = taskCommandUseCase.createTask(
                TaskRequestMapper.toCommand(request)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Task created successfully", response)
                );
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTaskById(@PathVariable Long taskId) {
        TaskResponseDto response = taskQueryUseCase.getTaskById(taskId);

        return ResponseEntity.ok(
                ApiResponse.success("Task retrieved successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskResponseDto>>> getTasks(
            @RequestParam(name = "page", defaultValue = "0")
            int page,

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TaskResponseDto>> uploadFile(
            @RequestParam String taskName,
            @RequestParam(required = false) String description,
            @RequestParam TaskType taskType,
            @RequestParam TaskPriority priority,
            @RequestParam String createdBy,
            @RequestParam MultipartFile file
    ) {
        UploadTaskCommand command = new UploadTaskCommand(
                taskName,
                description,
                taskType,
                priority,
                createdBy
        );

        TaskResponseDto response = taskCommandUseCase.uploadTask(command, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success("Task uploaded successfully", response)
                );
    }

//    @PostMapping("{taskId}/process")
//    public ResponseEntity<ApiResponse<TaskProcessingResponseDto>> processTask(@PathVariable Long taskId){
//        TaskProcessingResponseDto response = taskCommandUseCase.processTask(taskId);
//        return ResponseEntity.ok(
//                ApiResponse.success("Task processed successfully",response)
//        );
//    }
}
