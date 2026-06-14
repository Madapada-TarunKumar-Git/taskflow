package com.example.taskflow.domain.model;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.domain.exception.InvalidTaskStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    public void shouldCreateTaskWithCreatedStatus() {
        Task task = createTask();
        assertEquals(TaskStatus.CREATED, task.getStatus());
        assertEquals(0, task.getRetryCount());
        assertNotNull(task.getCreatedAt());
    }

    @Test
    public void shouldMoveFromCreatedToFileUpload() {
        Task task = createTask();

        task.markAsFileUploaded();
        assertEquals(TaskStatus.FILE_UPLOADED, task.getStatus());
    }

    @Test
    public void shouldMoveFromFileUploadToQueued() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        assertEquals(TaskStatus.QUEUED, task.getStatus());
    }

    @Test
    public void shouldMoveFromQueuedToProcessing() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        assertEquals(TaskStatus.PROCESSING, task.getStatus());
    }

    @Test
    public void shouldMoveFromProcessingToCompleted() {
        Task task = createTask();

        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsCompleted();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    public void shouldMoveFromProcessingToPartiallyCompleted() {
        Task task = createTask();

        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsPartiallyCompleted();

        assertEquals(TaskStatus.PARTIALLY_COMPLETED, task.getStatus());
    }

    @Test
    public void shouldMoveFromProcessingToRetryPending() {
        Task task = createTask();

        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsRetryPending();
        assertEquals(TaskStatus.RETRY_PENDING, task.getStatus());
    }

    @Test
    public void shouldMoveFromRetryPendingToQueued() {
        Task task = createTask();

        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsRetryPending();
        task.markAsQueued();
        assertEquals(TaskStatus.QUEUED, task.getStatus());
    }

    @Test
    public void shouldMoveFromProcessingToPermanentFailure() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsPermanentFailure("Permanent failure due to invalid data");
        assertEquals(TaskStatus.PERMANENT_FAILURE, task.getStatus());
    }

    @Test
    public void shouldIncrementRetryCount() {
        Task task = createTask();

        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.incrementRetryCount();
        assertEquals(1, task.getRetryCount());
    }

    @Test
    public void shouldReachMaxRetryLimit() {
        Task task = createTask();
        task.incrementRetryCount();
        task.incrementRetryCount();
        task.incrementRetryCount();
        assertEquals(3, task.getRetryCount());
        assertTrue(task.hasReachedMaxRetryLimit());
    }

    @Test
    public void shouldNotReachMaxRetryLimit(){
        Task task = createTask();
        task.incrementRetryCount();
        task.incrementRetryCount();

        assertFalse(task.hasReachedMaxRetryLimit());
    }


    @Test
    public void shouldThrowWhenFileUploadedFromQueued() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        assertThrows(InvalidTaskStateException.class, task::markAsFileUploaded);
    }

    @Test
    public void shouldThrowWhenCompletedFromCreated() {
        Task task = createTask();

        assertThrows(InvalidTaskStateException.class, task::markAsCompleted);
    }

    @Test
    public void shouldThrowWhenProcessingFromFileUploaded() {
        Task task = createTask();
        task.markAsFileUploaded();

        assertThrows(InvalidTaskStateException.class, task::markAsProcessing);
    }

    @Test
    public void shouldThrowWhenFileUploadingAgainFromProcessing() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();

        assertThrows(InvalidTaskStateException.class, task::markAsFileUploaded);
    }

    @Test
    public void shouldThrowWhenRetryPendingFromCompleted() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsCompleted();

        assertThrows(InvalidTaskStateException.class, task::markAsRetryPending);
    }

    @Test
    public void shouldThrowWhenQueuedFromCompleted() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsCompleted();

        assertThrows(InvalidTaskStateException.class, task::markAsQueued);
    }

    @Test
    public void shouldThrowWhenPermanentFailureFromCompleted() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsCompleted();

        assertThrows(InvalidTaskStateException.class, () -> task.markAsPermanentFailure("test"));
    }

    @Test
    public void shouldThrowWhenQueuedFromCreated() {
        Task task = createTask();

        assertThrows(InvalidTaskStateException.class, task::markAsQueued);
    }

    @Test
    public void shouldSetProcessingStartTime() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();

        assertNotNull(task.getProcessingStartedAt());
    }

    @Test
    public void shouldSetProcessingCompletedTime() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsCompleted();

        assertNotNull(task.getProcessingCompletedAt());
    }

    @Test
    public void shouldSetProcessingTimestamps(){
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsCompleted();

        assertNotNull(task.getProcessingStartedAt());
        assertNotNull(task.getProcessingCompletedAt());

        assertTrue(task.getProcessingCompletedAt().isAfter(task.getProcessingStartedAt())
                            || task.getProcessingCompletedAt().equals(task.getProcessingStartedAt()));
    }
    @Test
    public void shouldUpdateStatistics() {
        Task task = createTask();
        task.updateProcessingStatistics(100, 95, 5);

        assertEquals(100, task.getTotalRecords());
        assertEquals(95, task.getSuccessRecords());
        assertEquals(5, task.getFailedRecords());
    }

    @Test
    public void shouldAllowRetryFlowFromPermanentFailure() {
        Task task = createTask();
        task.markAsFileUploaded();
        task.markAsQueued();
        task.markAsProcessing();
        task.markAsPermanentFailure("");

        task.markAsFileUploaded();
        assertEquals(TaskStatus.FILE_UPLOADED, task.getStatus());

        task.markAsQueued();
        task.markAsProcessing();

        task.markAsRetryPending();
        assertEquals(TaskStatus.RETRY_PENDING, task.getStatus());
    }

    private Task createTask() {
        return new Task(
                "Import customer",
                "Customer import",
                TaskType.CUSTOMER_IMPORT,
                TaskPriority.HIGH,
                "tester"
        );
    }
}
