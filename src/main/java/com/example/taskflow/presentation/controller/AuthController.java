package com.example.taskflow.presentation.controller;

import com.example.taskflow.infrastructure.security.jwt.JwtService;
import com.example.taskflow.infrastructure.security.repo.UserEntityRepository;
import com.example.taskflow.infrastructure.security.service.UserService;
import com.example.taskflow.presentation.request.AuthRequest;
import com.example.taskflow.presentation.request.RegisterRequest;
import com.example.taskflow.presentation.response.AuthResponse;
import com.example.taskflow.presentation.response.RegisterResponse;
import com.example.taskflow.shared.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Login using username and password")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserEntityRepository repository;
    private final UserService userService;

    @Operation(summary = "Register user", description = "Create/register a new user with username, password and role")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @PostMapping("/register")
    public ResponseEntity<APIResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("User register request received. username = {}", registerRequest.username());
        RegisterResponse response = userService.register(registerRequest);
        log.info("User registered successfully. user Id = {}", response.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success("User registered successfully", response));
    }

    @Operation(summary = "User login", description = "Login with username and password to get JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        log.info("Log in request received. username = {}", request.username());
        AuthResponse response = userService.login(request);
        log.info("Log in successful. username = {}", request.username());
        return ResponseEntity.ok(APIResponse.success("Login successful", response));
    }
}
