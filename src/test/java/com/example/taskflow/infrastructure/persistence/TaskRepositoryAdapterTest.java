package com.example.taskflow.infrastructure.persistence;


import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.infrastructure.persistence.adapter.TaskRepositoryAdapter;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import com.example.taskflow.infrastructure.persistence.mapper.TaskEntityMapper;
import com.example.taskflow.infrastructure.persistence.repository.JpaTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskRepositoryAdapterTest {
    @InjectMocks
    TaskRepositoryAdapter taskRepositoryAdapter;

    @Mock
    JpaTaskRepository jpaTaskRepository;

    @Test
    public void shouldSaveTask(){
        Task task = createTask();
        TaskEntity entity = TaskEntityMapper.toEntity(task);

        when(jpaTaskRepository.save(any(TaskEntity.class))).thenReturn(entity);
        Task savedTask = taskRepositoryAdapter.save(task);
        assertEquals(TaskStatus.CREATED,savedTask.getStatus());
    }

    private Task createTask(){
        return new Task(
                "Customer Import",
                "Import customer details",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.MEDIUM,
                "tester"
        );
    }
}
