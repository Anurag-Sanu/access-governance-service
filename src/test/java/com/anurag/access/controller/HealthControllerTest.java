package com.anurag.access.controller;

import com.anurag.access.config.SecurityConfig;
import com.anurag.access.security.CustomAuthenticationEntryPoint;
import com.anurag.access.security.CustomUserDetailsService;
import com.anurag.access.security.JwtAuthenticationFilter;
import com.anurag.access.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class
})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldReturnHealthStatus() throws Exception {

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service")
                        .value("access-governance-service"));
    }
}
