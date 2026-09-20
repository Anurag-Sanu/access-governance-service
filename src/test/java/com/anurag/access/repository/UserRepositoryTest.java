package com.anurag.access.repository;

import com.anurag.access.entity.Role;
import com.anurag.access.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAllInBatch();
    }

    @Test
    void shouldSaveAndFindUserByEmail() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(Role.USER);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("test@example.com");
        assertThat(result.get().getRole())
                .isEqualTo(Role.USER);
    }

    @Test
    void shouldCheckIfEmailExists() {

        User user = new User();
        user.setEmail("existing@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(Role.USER);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);

        assertThat(
                userRepository.existsByEmail("existing@example.com")
        ).isTrue();

        assertThat(
                userRepository.existsByEmail("missing@example.com")
        ).isFalse();
    }
}