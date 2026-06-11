package com.example.taskflow.infrastructure.security.service;

import com.example.taskflow.infrastructure.security.entity.UserEntity;
import com.example.taskflow.infrastructure.security.enums.UserRoles;
import com.example.taskflow.infrastructure.security.repo.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize default users if they don't exist
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Check if admin user exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Set.of(UserRoles.ROLE_ADMIN));
            userRepository.save(admin);
            log.info("Default admin user created");
        }

        // Check if test user exists
        if (userRepository.findByUsername("user").isEmpty()) {
            UserEntity testUser = new UserEntity();
            testUser.setUsername("user");
            testUser.setPassword(passwordEncoder.encode("user123"));
            testUser.setRole(Set.of(UserRoles.ROLE_USER));
            userRepository.save(testUser);
            log.info("Default test user created");
        }
    }
}
