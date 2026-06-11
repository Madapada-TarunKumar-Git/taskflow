package com.example.taskflow.presentation.advice;

import com.example.taskflow.domain.exception.InvalidTaskStateException;
import com.example.taskflow.domain.exception.MaxRetryLimitExceededException;
import com.example.taskflow.domain.exception.TaskNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(
                        Collectors.toMap(
                                fieldError -> fieldError.getField(),
                                fieldError -> fieldError.getDefaultMessage()
                        )
                );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed"
        );
        problemDetail.setTitle("Request Validation Failed");
        problemDetail.setType(URI.create("/erros/validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(InvalidTaskStateException.class)
    public ProblemDetail handleInvalidTaskStateException(InvalidTaskStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage()
        );
        problemDetail.setTitle("Invalid Task State");
        problemDetail.setType(URI.create("/errors/task-state"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MaxRetryLimitExceededException.class)
    public ProblemDetail handleMaxRetryException(MaxRetryLimitExceededException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage()
        );
        problemDetail.setTitle("Retry Limit Exceeded");
        problemDetail.setType(URI.create("/errors/retry-limit"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFoundException(TaskNotFoundException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Task Not Found");
        problemDetail.setType(URI.create("/error/task-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid/Bad Request");
        problemDetail.setType(URI.create("/errors/bad-request"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("Authorization Denied");
        problemDetail.setType(URI.create("/errors/forbidden"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredJwtException(ExpiredJwtException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Expired JWT Token");
        problemDetail.setType(URI.create("/errors/expired-token"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFoundException(NoHandlerFoundException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail("Invalid API endpoint: " + ex.getRequestURL());
        problemDetail.setType(URI.create("/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("/errors/bad-request"));
        problemDetail.setProperty("timestamp",Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal server error"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
