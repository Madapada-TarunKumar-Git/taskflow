package com.example.taskflow.integration;

import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.dto.TaskProcessingResultDto;
import com.example.taskflow.application.service.CustomerCsvProcessingService;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import com.example.taskflow.presentation.response.TaskResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class TaskWorkFlowIntegrationTest {

    @Autowired
    TaskCommandUseCase taskCommandUseCase;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private CustomerCsvProcessingService csvProcessingService;

    @Test
    @WithMockUser(username = "tester")
    void shouldProcessTaskSuccessfully() {

        UploadTaskCommand command = new UploadTaskCommand(
                "Customer Import",
                "Importing customer details",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.MEDIUM
        );

        MultipartFile file = new MockMultipartFile(
                "file",
                "customers.csv",
                "text/csv",
                    """
                        id,name
                        1,Tarun
                    """
                        .getBytes());

        when(csvProcessingService.process(any())).thenReturn(new TaskProcessingResultDto(
                100,
                100,
                0,
                List.of()
        ));

        TaskResponseDto responseDto = taskCommandUseCase.uploadTask(command, file);

        Long taskId = responseDto.taskId();

        Task task = taskRepository.findById(taskId).orElseThrow();

        assertEquals("tester", task.getCreatedBy());
        assertEquals(TaskStatus.QUEUED, task.getStatus());

        Task uploadedTask = taskRepository.findById(task.getId()).orElseThrow();
        uploadedTask.markAsProcessing();
        taskRepository.save(uploadedTask);

        taskCommandUseCase.processTask(uploadedTask.getId());

        Task processedTask = taskRepository.findById(taskId).orElseThrow();

        assertEquals(100, processedTask.getTotalRecords());
        assertEquals(0, processedTask.getFailedRecords());
        assertEquals(TaskStatus.COMPLETED, processedTask.getStatus());

        verify(csvProcessingService).process(any());

    }
}
