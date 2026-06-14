package com.example.taskflow.application.service;

import com.example.taskflow.application.command.CreateTaskCommand;
import com.example.taskflow.application.command.UploadTaskCommand;
import com.example.taskflow.application.dto.TaskProcessingResultDto;
import com.example.taskflow.application.mapper.TaskMapper;
import com.example.taskflow.application.port.FileStoragePort;
import com.example.taskflow.application.port.FileUploadResult;
import com.example.taskflow.application.validation.TaskFileValidator;
import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.exception.InvalidTaskStateException;
import com.example.taskflow.domain.exception.TaskNotFoundException;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.shared.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskCommandServiceTest {
    @Mock
    TaskRepository taskRepository;
    @Mock
    FileStoragePort fileStoragePort;
    @Mock
    TaskFileValidator taskFileValidator;
    @Mock
    CustomerCsvProcessingService customerCsvProcessingService;
    @Mock
    TaskAuditService taskAuditService;
    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    TaskCommandService taskCommandService;

    /*
    A good target for your TaskCommandService is around 8–12 tests covering:

        1. createTask success
        2. uploadTask success
        3. uploadTask validation failure
        4. processTask success
        5. processTask partial success
        6. processTask retry pending
        7. processTask permanent failure
        8. reProcessFailedTask success
        9. reProcessFailedTask task not found
        10. reProcessFailedTask invalid state
     */

    @Test
    public void shouldCreateTaskSuccessfully() {
        CreateTaskCommand command = new CreateTaskCommand(
                "Import test",
                "Test description",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH
        );

        when(securityUtil.getUsername()).thenReturn("tester");
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        TaskResponseDto responseDto = taskCommandService.createTask(command);

        assertEquals("Import test", responseDto.taskName());
        assertEquals(TaskStatus.CREATED, responseDto.status());
        assertEquals(TaskPriority.HIGH, responseDto.priority());
        assertEquals("tester", responseDto.createdBy());

        verify(taskRepository).save(any(Task.class));
        verify(taskAuditService).logTaskEvent(
                any(Task.class),
                eq(TaskAuditAction.TASK_CREATED), //we use "eq" for real values
                isNull(),
                eq(TaskStatus.CREATED),
                eq("Task created"),
                eq("tester")
        );
    }

    @Test
    public void shouldUploadSuccessfully() {
        UploadTaskCommand command = validUploadTaskCommand();

        MultipartFile file = mock(MultipartFile.class);

        doNothing().when(taskFileValidator).validate(file, command.taskType());

        FileUploadResult result = fileUploadResult();

        when(securityUtil.getUsername()).thenReturn("tester");

        when(fileStoragePort.storeFile(file)).thenReturn(result);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        TaskResponseDto responseDto = taskCommandService.uploadTask(command, file);

        assertNotNull(responseDto);
        assertEquals("Customer upload", responseDto.taskName());
        assertEquals(TaskStatus.QUEUED, responseDto.status());
        assertEquals("customer.csv",result.originalFileName());
        assertEquals("tester", responseDto.createdBy());

        verify(taskRepository).save(any(Task.class));
        verify(taskFileValidator).validate(file, command.taskType());
        verify(fileStoragePort).storeFile(file);
        verify(taskAuditService, times(2)).logTaskEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void shouldFailWhenUploadValidationFails() {
        UploadTaskCommand command = inValidUploadTaskCommand();

        MultipartFile file = mock(MultipartFile.class);

        doThrow(new IllegalArgumentException())
                .when(taskFileValidator)
                .validate(file, command.taskType());

        assertThrows(IllegalArgumentException.class, () -> taskCommandService.uploadTask(command, file));

        verifyNoInteractions(fileStoragePort);
        verify(taskRepository, never()).save(any());
        verify(taskFileValidator).validate(file, command.taskType());

    }

    @Test
    void shouldThrowWhenFileStorageFails(){
        UploadTaskCommand command = validUploadTaskCommand();
        MultipartFile file = mock(MultipartFile.class);

        when(fileStoragePort.storeFile(file)).thenThrow(new RuntimeException("File storage failed"));

        assertThrows(RuntimeException.class, () -> taskCommandService.uploadTask(command,file));

        verify(taskRepository, never()).save(any());
    }

    @Test
    public void shouldProcessTaskSuccessfully() {

        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.setFilePath("uploads/123_customer.csv");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskProcessingResultDto resultDto = new TaskProcessingResultDto(
                100,
                100,
                0,
                List.of()
        );

        when(customerCsvProcessingService.process(task.getFilePath())).thenReturn(resultDto);

        taskCommandService.processTask(1L);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(100, task.getTotalRecords());
        assertEquals(100, task.getSuccessRecords());
        assertEquals(0, task.getFailedRecords());

        verify(customerCsvProcessingService).process(task.getFilePath());
        verify(taskAuditService).logTaskEvent(
                any(Task.class),
                eq(TaskAuditAction.TASK_COMPLETED),
                eq(TaskStatus.PROCESSING),
                eq(TaskStatus.COMPLETED),
                any(),
                eq("SYSTEM")
        );
        verify(taskRepository).save(any());
    }

    @Test
    public void shouldProcessTaskPartially() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.setFilePath("uploads/123_customer.csv");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskProcessingResultDto resultDto = new TaskProcessingResultDto(
                100,
                98,
                2,
                List.of()
        );

        when(customerCsvProcessingService.process(task.getFilePath())).thenReturn(resultDto);

        taskCommandService.processTask(1L);

        assertEquals(TaskStatus.PARTIALLY_COMPLETED, task.getStatus());
        assertEquals(100, task.getTotalRecords());
        assertEquals(98, task.getSuccessRecords());
        assertEquals(2, task.getFailedRecords());

        verify(customerCsvProcessingService).process(task.getFilePath());
        verify(taskAuditService).logTaskEvent(
                any(Task.class),
                eq(TaskAuditAction.PROCESSING_PARTIALLY_COMPLETED),
                eq(TaskStatus.PROCESSING),
                eq(TaskStatus.PARTIALLY_COMPLETED),
                eq("Processing partially completed"),
                eq("SYSTEM")
        );
        verify(taskRepository).save(any(Task.class));
    }


    @Test
    public void shouldProcessTaskRetryPending() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.setFilePath("uploads/123_customer.csv");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        when(customerCsvProcessingService.process(task.getFilePath())).thenThrow(RuntimeException.class);

        taskCommandService.processTask(1L);

        assertEquals(TaskStatus.RETRY_PENDING,task.getStatus());
        assertEquals(1,task.getRetryCount());
        assertFalse(task.hasReachedMaxRetryLimit());

        verify(customerCsvProcessingService).process(any());
        verify(taskAuditService).logTaskEvent(
                any(Task.class),
                eq(TaskAuditAction.RETRY_TRIGGERED),
                eq(TaskStatus.PROCESSING),
                eq(TaskStatus.RETRY_PENDING),
                any(),
                eq("SYSTEM")
        );

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void shouldMoveTaskToPermanentFailureWhenMaxRetriesReached(){
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.setFilePath("uploads/123_customer.csv");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        when(customerCsvProcessingService.process(task.getFilePath())).thenThrow(RuntimeException.class);
        task.incrementRetryCount();
        task.incrementRetryCount();

        taskCommandService.processTask(1L);

        assertEquals(TaskStatus.PERMANENT_FAILURE,task.getStatus());
        assertTrue(task.hasReachedMaxRetryLimit());

        verify(customerCsvProcessingService).process(task.getFilePath());
        verify(taskAuditService).logTaskEvent(
                any(Task.class),
                eq(TaskAuditAction.TASK_FAILED),
                eq(TaskStatus.PROCESSING),
                eq(TaskStatus.PERMANENT_FAILURE),
                any(),
                eq("SYSTEM")
        );
        verify(taskRepository).save(any(Task.class));

    }

    @Test
    public void shouldReprocessPermanentFailureTaskToQueue() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsPermanentFailure("Failed");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(securityUtil.getUsername()).thenReturn("Tarun");
        MultipartFile file = mock(MultipartFile.class);

        FileUploadResult result = fileUploadResult();

        when(fileStoragePort.storeFile(file)).thenReturn(result);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        TaskResponseDto response = taskCommandService.reProcessFailedTask(1L, file);

        assertEquals(TaskStatus.QUEUED, task.getStatus());
        assertEquals("customer.csv", task.getOriginalFileName());
        assertEquals("123_customer.csv", task.getStoredFileName());
        assertEquals(TaskStatus.QUEUED, response.status());

        verify(taskFileValidator).validate(file, task.getTaskType());
        verify(fileStoragePort).storeFile(file);
        verify(taskRepository).save(task);
        verify(taskAuditService, times(2)).logTaskEvent(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    public void shouldThrowWhenReprocessFailedTaskWhenTaskNotFound(){
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        MultipartFile file = mock(MultipartFile.class);

        assertThrows(TaskNotFoundException.class, () -> taskCommandService.reProcessFailedTask(1L,file));
    }

    @Test
    public void shouldThrowInvalidStateWhenTaskMoveFromQueue() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        MultipartFile file = mock(MultipartFile.class);

        assertThrows(InvalidTaskStateException.class, () -> taskCommandService.reProcessFailedTask(1L, file));
    }

    private FileUploadResult fileUploadResult() {
        return new FileUploadResult(
                "customer.csv",
                "123_customer.csv",
                "/uploads/123_customer.csv",
                100L);
    }

    private UploadTaskCommand validUploadTaskCommand() {
        return new UploadTaskCommand(
                "Customer upload",
                "upload customer details",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.MEDIUM
        );
    }

    private UploadTaskCommand inValidUploadTaskCommand() {
        return new UploadTaskCommand(
                "Customer upload",
                "upload customer details",
                TaskType.BILL_PROCESSING,
                TaskPriority.MEDIUM
        );
    }

    private Task createTask() {
        return new Task(
                "Customer Import",
                "Import customer details",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        );
    }
}
