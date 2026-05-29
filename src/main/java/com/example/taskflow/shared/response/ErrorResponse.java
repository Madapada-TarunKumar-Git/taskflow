package com.example.taskflow.shared.response;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {
    private boolean success;
    private String message;
    private List<FieldErrorResponse> errors;
    private Instant timestamp;
}
