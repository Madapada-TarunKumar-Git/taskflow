package com.example.taskflow.shared.response;

import java.time.Instant;

public record APIResponse<T> (
    boolean success,
    String message,
    T data,
    Instant timestamp
){
    public static <T> APIResponse<T> success(
            String message,
            T data
    ){
        return new APIResponse<>(
                true,
                message,
                data,
                Instant.now()
        );
    }
}
