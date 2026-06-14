package com.example.taskflow.application.service;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.model.TaskAudit;
import com.example.taskflow.domain.repository.TaskAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskAuditServiceTest {
    @InjectMocks
    TaskAuditService taskAuditService;

    @Mock
    TaskAuditRepository taskAuditRepository;

    @Test
    public void shouldSaveAuditEvents() {
        Task task = new Task(
                "customer import",
                "import customer",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        );

        ReflectionTestUtils.setField(task,"id",1L);

        taskAuditService.logTaskEvent(
                task,
                TaskAuditAction.TASK_QUEUED,
                TaskStatus.FILE_UPLOADED,
                TaskStatus.QUEUED,
                "task queued",
                "tester"
        );

        ArgumentCaptor<TaskAudit> auditArgsCaptor = ArgumentCaptor.forClass(TaskAudit.class);
        verify(taskAuditRepository).save(auditArgsCaptor.capture());

        TaskAudit savedAudit = auditArgsCaptor.getValue();

        assertEquals(TaskAuditAction.TASK_QUEUED,savedAudit.getAction());
        assertEquals(TaskStatus.FILE_UPLOADED, savedAudit.getFromStatus());
        assertEquals(TaskStatus.QUEUED, savedAudit.getToStatus());
        assertEquals("task queued",savedAudit.getMessage());
        assertEquals("tester",savedAudit.getPerformedBy());
    }
}
