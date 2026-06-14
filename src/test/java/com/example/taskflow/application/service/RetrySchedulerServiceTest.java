package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetrySchedulerServiceTest {
    @InjectMocks
    RetryScheduleService retryScheduleService;

    @Mock
    TaskRepository taskRepository;
    @Mock
    RetryTaskService retryTaskService;

    @Test
    public void shouldRetryPendingTasks() {


        List<Task> tasks = getRetryPendingTasks();

        when(taskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.RETRY_PENDING)).thenReturn(tasks);
        doNothing().when(retryTaskService).retryTask(anyLong());

        retryScheduleService.retryPendingTasks();

        verify(taskRepository).findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.RETRY_PENDING);
        verify(retryTaskService, times(tasks.size())).retryTask(anyLong());
    }

    private static List<Task> getRetryPendingTasks(){
        Task task1 = new Task(
                "Import customer",
                "Customer import",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        );

        task1.markAsFileUploaded();
        task1.markAsQueued();
        task1.markAsProcessing();
        task1.markAsRetryPending();
        ReflectionTestUtils.setField(task1,"id",1L);

        Task task2 = new Task(
                "Import customer",
                "Customer import",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.MEDIUM,
                "tester"
        );

        task2.markAsFileUploaded();
        task2.markAsQueued();
        task2.markAsProcessing();
        task2.markAsRetryPending();
        ReflectionTestUtils.setField(task2,"id",2L);

        return List.of(task1,task2);
    }
}

