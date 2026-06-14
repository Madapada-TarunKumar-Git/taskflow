package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.StatusResponse;
import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.exception.TaskNotFoundException;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.shared.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskQueryServiceTest {
    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskQueryService taskQueryService;

    @Test
    public void shouldGetTaskById() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task(
                "Import Customer",
                "Customer import",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        )));
        TaskResponseDto response = taskQueryService.getTaskById(1L);
        assertEquals("Import Customer", response.taskName());
        assertEquals(TaskStatus.CREATED, response.status());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals("tester", response.createdBy());

        verify(taskRepository).findById(1L);
    }

    @Test
    public void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskQueryService.getTaskById(1L));
        verify(taskRepository).findById(1L);
    }

    @Test
    public void shouldGetTasksWithPagination() {
        when(taskRepository.findTasks(any(), any())).thenReturn(Page.empty());
        PageResponse<TaskResponseDto> response = taskQueryService.getTasks(
                0,
                10,
                "createdAt",
                "desc",
                TaskStatus.PROCESSING
        );
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(taskRepository).findTasks(pageableCaptor.capture(), eq(TaskStatus.PROCESSING));
        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(0, response.content().size());

    }

    @Test
    public void shouldGetAllTasksWhenStatusNotProvided() {
        when(taskRepository.findTasks(any(), any())).thenReturn(Page.empty());

        taskQueryService.getTasks(
                0,
                10,
                "createdAt",
                "desc",
                null
        );

        verify(taskRepository).findTasks(any(Pageable.class), isNull());
    }

    @Test
    public void shouldGetStatistics() {
        when(taskRepository.countAll()).thenReturn(17L);
        when(taskRepository.countTasks(TaskStatus.COMPLETED)).thenReturn(8L);
        when(taskRepository.countTasks(TaskStatus.PROCESSING)).thenReturn(2L);
        when(taskRepository.countTasks(TaskStatus.PERMANENT_FAILURE)).thenReturn(2L);
        when(taskRepository.countTasks(TaskStatus.RETRY_PENDING)).thenReturn(1L);
        when(taskRepository.countTasks(TaskStatus.QUEUED)).thenReturn(3L);
        when(taskRepository.countTasks(TaskStatus.CREATED)).thenReturn(1L);

        StatusResponse response = taskQueryService.getTaskStatistics();

        assertEquals(17, response.totalTasks());
        assertEquals(8, response.completedTasks());
        assertEquals(2, response.processingTasks());
        assertEquals(2, response.failedTasks());
        assertEquals(1, response.retryPendingTasks());
        assertEquals(3, response.queuedTasks());
        assertEquals(1, response.createdTasks());

        verify(taskRepository).countAll();
        verify(taskRepository).countTasks(TaskStatus.COMPLETED);
        verify(taskRepository).countTasks(TaskStatus.PROCESSING);
        verify(taskRepository).countTasks(TaskStatus.PERMANENT_FAILURE);
        verify(taskRepository).countTasks(TaskStatus.RETRY_PENDING);
        verify(taskRepository).countTasks(TaskStatus.QUEUED);
        verify(taskRepository).countTasks(TaskStatus.CREATED);
    }


}
