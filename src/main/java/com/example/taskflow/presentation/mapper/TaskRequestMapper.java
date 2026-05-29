package com.example.taskflow.presentation.mapper;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.presentation.request.CreateTaskRequest;

public final class TaskRequestMapper {
    private TaskRequestMapper(){}

    public static CreateTaskCommand toCommand(
            CreateTaskRequest request
    ){
        return new CreateTaskCommand(
                request.taskName(),
                request.description(),
                request.taskType(),
                request.priority(),
                request.createdBy()
        );
    }
}
