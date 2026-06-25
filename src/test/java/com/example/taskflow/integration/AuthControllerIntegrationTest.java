package com.example.taskflow.integration;

import com.example.taskflow.TaskflowApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TaskflowApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldRegister() throws Exception {
        String registerRequest = """
                {
                    "username":"Tester",
                    "password":"password123",
                    "role":["ROLE_USER"]
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username")
                        .value("Tester")
                );
    }

    @Test
    void shouldLogin() throws Exception {
//        shouldRegister();
        String loginRequest = """
                {
                  "username": "Tester",
                  "password": "password123"
                }
                """;
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Login successful"));
    }

}
