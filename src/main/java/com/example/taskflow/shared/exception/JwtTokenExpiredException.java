package com.example.taskflow.shared.exception;

public class JwtTokenExpiredException extends JwtAuthenticationException {
    public JwtTokenExpiredException(String message) {
        super(message);
    }
}
