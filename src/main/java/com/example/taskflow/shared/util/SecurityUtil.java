package com.example.taskflow.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    public String getUsername(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
