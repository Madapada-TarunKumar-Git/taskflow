package com.example.taskflow.infrastructure.security.service;

import com.example.taskflow.infrastructure.security.entity.UserEntity;
import com.example.taskflow.infrastructure.security.jwt.JwtService;
import com.example.taskflow.infrastructure.security.repo.UserEntityRepository;
import com.example.taskflow.presentation.request.AuthRequest;
import com.example.taskflow.presentation.request.RegisterRequest;
import com.example.taskflow.presentation.response.AuthResponse;
import com.example.taskflow.presentation.response.RegisterResponse;
import com.example.taskflow.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserEntityRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (repository.findByUsername(registerRequest.username()).isPresent()) {
            log.info("Username {} already exists", registerRequest.username());
            throw new IllegalArgumentException("Username already exists");
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(registerRequest.username());
        entity.setPassword(passwordEncoder.encode(registerRequest.password()));
        entity.setRole(registerRequest.role());

        UserEntity savedEntity = repository.save(entity);

        return new RegisterResponse(
                savedEntity.getUserId(),
                savedEntity.getUsername(),
                savedEntity.getRole().toString()
        );
    }

    public AuthResponse login(AuthRequest request) {
        UserEntity entity = repository.findByUsername(request.username()).orElseThrow(() ->
                new ResourceNotFoundException("User not found with username: " + request.username()));
        String token = jwtService.generateToken(new User(
                entity.getUsername(),
                "",
                entity.getRole()
                        .stream()
                        .map(role ->
                                new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toSet())
        ));

        return new AuthResponse(token);
    }
}
