package com.example.taskflow.domain.exception;

public class MaxRetryLimitExceededException extends RuntimeException{
    public MaxRetryLimitExceededException(String message){
        super(message);
    }
}
