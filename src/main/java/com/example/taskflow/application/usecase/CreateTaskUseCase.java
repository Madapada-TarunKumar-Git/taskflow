package com.example.taskflow.application.usecase;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.dto.TaskResponseDto;

public interface CreateTaskUseCase {
    TaskResponseDto createTask(CreateTaskCommand command);
}
