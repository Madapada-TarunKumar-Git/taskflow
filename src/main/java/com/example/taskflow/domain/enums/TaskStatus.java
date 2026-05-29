package com.example.taskflow.domain.enums;

public enum TaskStatus {
    CREATED,
    VALIDATING,
    PROCESSING,
    COMPLETED,
    FAILED,
    RETRY_PENDING
}
