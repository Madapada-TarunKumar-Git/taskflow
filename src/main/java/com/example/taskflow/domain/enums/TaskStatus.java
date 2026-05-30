package com.example.taskflow.domain.enums;

public enum TaskStatus {
    CREATED,
    FILE_UPLOADED,
    VALIDATING,
    VALIDATION_FAILED,
    QUEUED,
    PROCESSING,
    COMPLETED,
    PARTIALLY_COMPLETED,
    FAILED,
    RETRY_PENDING
}
