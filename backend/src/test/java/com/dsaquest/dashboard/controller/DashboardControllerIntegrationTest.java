package com.dsaquest.dashboard.controller;

import com.dsaquest.auth.entity.Role;
import com.dsaquest.auth.entity.User;
import com.dsaquest.auth.repository.UserRepository;
import com.dsaquest.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "DSA_QUEST_DB_INTEGRATION", matches = "true")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void rejectsUnauthenticatedDashboardAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsOnlyTheAuthenticatedUsersEmptyDashboard() throws Exception {
        User user = userRepository.save(new User(
                UUID.randomUUID(),
                "Dashboard Integration User",
                "dashboard-integration-" + UUID.randomUUID() + "@example.test",
                "not-a-login-password",
                Role.STUDENT));
        String accessToken = jwtService.createAccessToken(user).value();

        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", is(user.getId().toString())))
                .andExpect(jsonPath("$.xp.totalXp", is(0)))
                .andExpect(jsonPath("$.xp.level", is(1)))
                .andExpect(jsonPath("$.streak.currentDays", is(0)))
                .andExpect(jsonPath("$.learning.publishedLessons", is(0)))
                .andExpect(jsonPath("$.continueLearning").doesNotExist());
    }
}
