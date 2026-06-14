package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class TaskSchedulerServiceTest {
    @InjectMocks
    TaskSchedulerService taskSchedulerService;

    @Mock
    TaskRepository taskRepository;
    @Mock
    AsyncTaskProcessorService asyncTaskProcessorService;
    @Mock
    TaskAuditService taskAuditService;

    @Test
    public void shouldScheduleQueuedTasks() {
        List<Task> tasks = getQueuedTasks();

        when(taskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.QUEUED)).thenReturn(tasks);

        // Simulate DB update: mark tasks as PROCESSING when claim succeeds
        when(taskRepository.claimTaskForProcessing(1L)).thenAnswer(inv -> {
            tasks.getFirst().markAsProcessing();
            return true;
        });
        when(taskRepository.claimTaskForProcessing(2L)).thenAnswer(inv -> {
            tasks.get(1).markAsProcessing();
            return true;
        });

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tasks.get(0)));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(tasks.get(1)));

        doNothing().when(asyncTaskProcessorService).processTaskAsync(anyLong());

        taskSchedulerService.processQueuedTasks();

        // Verify claim calls
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskRepository, times(2)).claimTaskForProcessing(idCaptor.capture());
        assertEquals(List.of(1L, 2L), idCaptor.getAllValues());

        // Verify async processor triggered
        verify(asyncTaskProcessorService, times(2)).processTaskAsync(anyLong());

        // Verify tasks are now PROCESSING
        assertEquals(TaskStatus.PROCESSING, tasks.get(0).getStatus());
        assertEquals(TaskStatus.PROCESSING, tasks.get(1).getStatus());
    }


    private static List<Task> getQueuedTasks(){
        Task task1 = new Task(
                "Import customer",
                "Customer import",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        );

        task1.markAsFileUploaded();
        task1.markAsQueued();
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
        ReflectionTestUtils.setField(task2,"id",2L);

        return List.of(task1,task2);
    }
}
