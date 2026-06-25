package com.example.taskflow.presentation.request;

import com.example.taskflow.infrastructure.security.enums.UserRoles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 16, message = "Password length must be 8 to 16 characters")
        String password,

        @NotNull(message = "Role is required")
        Set<UserRoles> role
) {
}
