package com.example.taskflow.application.usecase;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.presentation.response.TaskProcessingResponseDto;
import com.example.taskflow.presentation.response.TaskResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface TaskCommandUseCase {
    // for write operation services
    TaskResponseDto createTask(CreateTaskCommand command);
    TaskResponseDto uploadTask(UploadTaskCommand command, MultipartFile file);
    void processTask(Long taskId);
}
