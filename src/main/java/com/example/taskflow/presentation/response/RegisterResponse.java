package com.example.taskflow.presentation.response;

public record RegisterResponse (
        Long userId,
        String username,
        String role
){
}
