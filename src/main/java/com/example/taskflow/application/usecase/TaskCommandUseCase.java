package com.example.taskflow.application.usecase;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.presentation.response.CreateTaskResponseDto;
import com.example.taskflow.presentation.response.RetryTaskResponseDto;
import com.example.taskflow.presentation.response.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface TaskCommandUseCase {
    // for write operation services
    CreateTaskResponseDto createTask(CreateTaskCommand command);

    UploadResponseDto uploadTask(UploadTaskCommand command, MultipartFile file);

    void processTask(Long taskId);

    RetryTaskResponseDto reProcessFailedTask(Long taskId, MultipartFile file);
}
