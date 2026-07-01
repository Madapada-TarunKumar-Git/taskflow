package com.example.taskflow.integration;

import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.dto.TaskProcessingResultDto;
import com.example.taskflow.application.service.CustomerCsvProcessingService;
import com.example.taskflow.application.service.TaskSchedulerService;
import com.example.taskflow.application.usecase.TaskCommandUseCase;
import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class TaskSchedulerIntegrationTest {

    @Autowired
    private TaskSchedulerService schedulerService;

    @Autowired
    private TaskCommandUseCase taskCommandUseCase;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private CustomerCsvProcessingService csvProcessingService;

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    void shouldProcessQueuedTasksUsingScheduler() {
        when(csvProcessingService.process(any()))
                .thenReturn(new TaskProcessingResultDto(
                        100,
                        100,
                        0,
                        List.of()
                ));
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
        Long taskId = taskCommandUseCase.uploadTask(command,file).taskId();

        schedulerService.processQueuedTasks();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Task task = taskRepository.findById(taskId).orElseThrow();
                    assertEquals(TaskStatus.COMPLETED,task.getStatus());
                });
    }
}
