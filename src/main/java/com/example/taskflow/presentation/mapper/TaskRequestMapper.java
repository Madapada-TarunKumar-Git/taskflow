package com.example.taskflow.presentation.mapper;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.presentation.request.CreateTaskRequest;
import com.example.taskflow.presentation.request.UploadTaskRequest;

public final class TaskRequestMapper {
    private TaskRequestMapper(){}

    public static CreateTaskCommand toCreateCommand(CreateTaskRequest request){
        return new CreateTaskCommand(
                request.taskName(),
                request.description(),
                request.taskType(),
                request.priority()
        );
    }

    public static UploadTaskCommand toUploadCommand(UploadTaskRequest request){
        return new UploadTaskCommand(
                request.taskName(),
                request.description(),
                request.taskType(),
                request.priority()
        );
    }
}
