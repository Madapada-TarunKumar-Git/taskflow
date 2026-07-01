package com.example.taskflow.integration;

import com.example.taskflow.TaskflowApplication;
import com.example.taskflow.infrastructure.security.jwt.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;
import java.util.UUID;
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

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String username;

    @BeforeEach
    void setUp() throws Exception {
        username = "tester_" + UUID.randomUUID().toString().replace("-", "");

        String registerRequest = String.format("""
                {
                    "username": "%s",
                    "password": "password123",
                    "role": ["ROLE_USER"]
                }
                """, username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "password123"
                        }
                        """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());

        token = json.get("data").get("token").asText();
    }

    private Long createTask() throws Exception {
        String request = """
                 {
                    "taskName":"Import Customers",
                    "description":"Customer import",
                    "taskType":"CUSTOMER_IMPORT",
                    "priority":"HIGH"
                 }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.taskName")
                        .value("Import Customers"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        return json.get("data").get("taskId").asLong();
    }

    @Test
    void shouldCreateTask() throws Exception {
        createTask();
    }

    @Test
    void shouldGetTaskById() throws Exception {
        Long taskId = createTask();
        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.taskName").value("Import Customers"));
    }
}
