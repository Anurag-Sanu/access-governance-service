package com.anurag.access.service;

import com.anurag.access.dto.user.CreateUserRequest;
import com.anurag.access.dto.user.UserResponse;
import com.anurag.access.entity.Role;
import com.anurag.access.entity.User;
import com.anurag.access.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash("password123");
        savedUser.setRole(Role.USER);

        LocalDateTime now = LocalDateTime.now();

        savedUser.setCreatedAt(now);
        savedUser.setUpdatedAt(now);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail())
                .isEqualTo("test@example.com");
        assertThat(response.getRole())
                .isEqualTo(Role.USER);

        verify(userRepository)
                .existsByEmail("test@example.com");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@example.com"))
                .thenReturn(true);

        assertThatThrownBy(
                () -> userService.createUser(request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with this email already exists");

        verify(userRepository)
                .existsByEmail("existing@example.com");

        verify(userRepository, never())
                .save(any(User.class));
    }
}
