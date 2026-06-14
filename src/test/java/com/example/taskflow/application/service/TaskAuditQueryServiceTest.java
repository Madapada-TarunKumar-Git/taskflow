package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.AuditResponse;
import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.TaskAudit;
import com.example.taskflow.domain.repository.TaskAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskAuditQueryServiceTest {
    @InjectMocks
    TaskAuditQueryService taskAuditQueryService;

    @Mock
    TaskAuditRepository taskAuditRepository;

    @Test
    public void shouldGetTaskAuditEvents(){
        List<TaskAudit> audits = List.of(
                new TaskAudit(1L, TaskAuditAction.TASK_CREATED,null, TaskStatus.CREATED,"task created","tester"),
                new TaskAudit(1L, TaskAuditAction.FILE_UPLOADED,TaskStatus.CREATED, TaskStatus.FILE_UPLOADED,"file uploaded","tester"),
                new TaskAudit(1L, TaskAuditAction.TASK_QUEUED,TaskStatus.FILE_UPLOADED, TaskStatus.QUEUED,"task queued","tester"),
                new TaskAudit(1L, TaskAuditAction.PROCESSING_STARTED,TaskStatus.QUEUED, TaskStatus.PROCESSING,"task processing","tester"),
                new TaskAudit(1L, TaskAuditAction.PROCESSING_COMPLETED,TaskStatus.PROCESSING, TaskStatus.COMPLETED,"task completed","tester")
        );

        when(taskAuditRepository.findByTaskId(1L)).thenReturn(audits);

        List<AuditResponse> response = taskAuditQueryService.getTaskAudits(1L);

        assertEquals("tester",response.getFirst().performedBy());
        assertEquals(TaskAuditAction.TASK_CREATED,response.getFirst().action());
        assertEquals(TaskAuditAction.PROCESSING_COMPLETED,response.getLast().action());
        assertEquals("task completed",response.getLast().message());
        assertEquals(5,response.size());

        verify(taskAuditRepository).findByTaskId(1L);
    }
}
