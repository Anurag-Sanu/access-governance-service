package com.anurag.access.controller;

import com.anurag.access.config.SecurityConfig;
import com.anurag.access.dto.user.UserResponse;
import com.anurag.access.entity.Role;
import com.anurag.access.exception.DuplicateUserException;
import com.anurag.access.exception.GlobalExceptionHandler;
import com.anurag.access.security.CustomAuthenticationEntryPoint;
import com.anurag.access.security.CustomUserDetailsService;
import com.anurag.access.security.JwtAuthenticationFilter;
import com.anurag.access.security.JwtService;
import com.anurag.access.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldCreateUser() throws Exception {

        UserResponse response = new UserResponse(
                1L,
                "test@example.com",
                Role.USER,
                LocalDateTime.now()
        );

        when(userService.createUser(any()))
                .thenReturn(response);

        String request = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("test@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("USER"));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        String request = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectShortPassword() throws Exception {

        String request = """
                {
                    "email": "test@example.com",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(new DuplicateUserException(
                        "User with this email already exists"
                ));

        String request = """
                {
                    "email": "existing@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"));
    }
}
