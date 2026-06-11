package com.example.taskflow.presentation.controller;

import com.example.taskflow.infrastructure.security.entity.UserEntity;
import com.example.taskflow.infrastructure.security.jwt.JwtService;
import com.example.taskflow.infrastructure.security.repo.UserEntityRepository;
import com.example.taskflow.presentation.request.AuthRequest;
import com.example.taskflow.presentation.response.AuthResponse;
import com.example.taskflow.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Login using username and password")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserEntityRepository repository;

    @Operation(summary = "User login", description = "Login with username and password to get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    })
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));
        UserEntity userEntity = repository.findByUsername(request.username()).orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        String token = jwtService.generateToken(User
                .withUsername(userEntity.getUsername())
                .password("")
                .authorities(userEntity.getRole().toString())
                .build()
        );

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
