package com.anurag.access.repository;

import com.anurag.access.entity.Role;
import com.anurag.access.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {

        String email = "test-" + UUID.randomUUID() + "@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setRole(Role.USER);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail(email);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo(email);
        assertThat(result.get().getRole())
                .isEqualTo(Role.USER);
    }

    @Test
    void shouldCheckIfEmailExists() {

        String email = "test-" + UUID.randomUUID() + "@example.com";
        String missingEmail = "missing-" + UUID.randomUUID() + "@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setRole(Role.USER);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.saveAndFlush(user);

        assertThat(
                userRepository.existsByEmail(email)
        ).isTrue();

        assertThat(
                userRepository.existsByEmail(missingEmail)
        ).isFalse();
    }
}