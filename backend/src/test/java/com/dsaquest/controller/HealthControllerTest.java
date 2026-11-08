package com.dsaquest.controller;

import com.dsaquest.auth.repository.UserRepository;
import com.dsaquest.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsStructuredHealthResponse() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application", is("dsa-quest")))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
