package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.dto.TaskResponseDto;
import com.example.taskflow.application.usecase.CreateTaskUseCase;
import com.example.taskflow.presentation.mapper.TaskRequestMapper;
import com.example.taskflow.presentation.request.CreateTaskRequest;
import com.example.taskflow.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final CreateTaskUseCase createTaskUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTasks(
            @Valid CreateTaskRequest request
            ){
        TaskResponseDto response = createTaskUseCase.createTask(
                TaskRequestMapper.toCommand(request)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Task created successfully",
                                response
                        )
                );
    }
}
