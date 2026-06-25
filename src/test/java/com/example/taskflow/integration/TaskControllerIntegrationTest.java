package com.example.taskflow.integration;

import com.example.taskflow.TaskflowApplication;
import com.example.taskflow.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = TaskflowApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void setToken() throws Exception {
        String registerRequest = """
                {
                    "username": "Tester",
                    "password": "password123",
                    "role": ["ROLE_USER"]
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
        );
        String loginRequest = """
                {
                  "username": "Tester",
                  "password": "password123"
                }
                """;
//        MvcResult token = mockMvc.perform(post("/api/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(loginRequest))
//                .andReturn();

        token = jwtService.generateToken(new User(
                "Tester",
                "",
                Set.of("ROLE_USER", "ROLE_ADMIN")
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()))
        );
    }

    @Test
    void shouldCreateTask() throws Exception {
        String request = """
                 {
                    "taskName":"Import Customers",
                    "description":"Customer import",
                    "taskType":"CUSTOMER_IMPORT",
                    "priority":"HIGH"
                 }
                """;

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(request)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.taskName")
                        .value("Import Customers"));
    }

    @Test
    void shouldGetTaskById() throws Exception{
        shouldCreateTask();
        mockMvc.perform(get("/api/v1/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + token)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskName")
                        .value("Import Customers"));
    }
}
