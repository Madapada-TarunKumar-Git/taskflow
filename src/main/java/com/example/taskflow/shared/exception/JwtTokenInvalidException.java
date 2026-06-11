package com.example.taskflow.shared.exception;

public class JwtTokenInvalidException extends JwtAuthenticationException {
    public JwtTokenInvalidException(String  message) {
        super(message);
    }
}
