package com.anurag.access.service;

import com.anurag.access.dto.user.CreateUserRequest;
import com.anurag.access.dto.user.UserResponse;
import com.anurag.access.entity.Role;
import com.anurag.access.entity.User;
import com.anurag.access.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.anurag.access.exception.DuplicateUserException;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException(
                    "User with this email already exists"
            );
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setRole(Role.USER);

        LocalDateTime now = LocalDateTime.now();

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
