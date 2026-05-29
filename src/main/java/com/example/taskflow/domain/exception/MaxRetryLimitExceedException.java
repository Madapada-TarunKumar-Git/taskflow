package com.example.taskflow.domain.exception;

public class MaxRetryLimitExceedException extends RuntimeException {
    public MaxRetryLimitExceedException(String message) {
        super(message);
    }
}
