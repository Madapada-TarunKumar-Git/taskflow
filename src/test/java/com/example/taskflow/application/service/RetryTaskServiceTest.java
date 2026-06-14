package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RetryTaskServiceTest {
    @Mock
    TaskRepository taskRepository;
    @Mock
    TaskAuditService taskAuditService;

    @InjectMocks
    RetryTaskService retryTaskService;

    @Test
    public void shouldMoveRetryPendingTaskToQueued() {
        Task task = new Task("Import customer",
                "Customer import",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        );

        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsRetryPending();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        retryTaskService.retryTask(1L);

        ArgumentCaptor<Task> taskArgsCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskArgsCaptor.capture());

        Task savedTask = taskArgsCaptor.getValue();

        assertEquals(TaskStatus.QUEUED, savedTask.getStatus());
        assertEquals("tester",savedTask.getCreatedBy());

        verify(taskRepository).findById(1L);
        verify(taskAuditService).logTaskEvent(
                any(Task.class),
                eq(TaskAuditAction.TASK_RE_QUEUED),
                eq(TaskStatus.RETRY_PENDING),
                eq(savedTask.getStatus()),
                any(),
                any()
        );
    }
}
