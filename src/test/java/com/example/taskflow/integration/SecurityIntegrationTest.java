package com.example.taskflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenNoAuthentication() throws Exception{
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @WithMockUser(username = "tester")
    void shouldReturn403WhenUserAccessAdminEndpoint() throws Exception{
        mockMvc.perform(get("/api/v1/tasks/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAdminToAccessStatistics() throws Exception{
        mockMvc.perform(get("/api/v1/tasks/statistics"))
                .andExpect(status().isOk());
    }
}
