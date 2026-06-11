package com.example.taskflow.shared.exception;

import org.springframework.security.core.AuthenticationException;

public abstract class JwtAuthenticationException extends AuthenticationException {
    protected JwtAuthenticationException(String message) {
        super(message);
    }
}
