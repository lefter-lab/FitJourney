package com.fitjourney.fitjourney.repository;

import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUsername_shouldReturnSavedUser() {
        User savedUser = userRepository.saveAndFlush(user("johnny", "john@example.com", UserRole.USER));

        Optional<User> result = userRepository.findByUsername("johnny");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        assertThat(result.get().getUsername()).isEqualTo("johnny");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getPassword()).isEqualTo("encoded-password");
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
        assertThat(result.get().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void findByEmail_shouldReturnSavedUser() {
        User savedUser = userRepository.saveAndFlush(user("trainer", "trainer@example.com", UserRole.TRAINER));

        Optional<User> result = userRepository.findByEmail("trainer@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        assertThat(result.get().getUsername()).isEqualTo("trainer");
        assertThat(result.get().getEmail()).isEqualTo("trainer@example.com");
        assertThat(result.get().getRole()).isEqualTo(UserRole.TRAINER);
    }

    @Test
    void findByUsernameAndFindByEmail_shouldReturnEmptyWhenMissing() {
        assertThat(userRepository.findByUsername("missing")).isEmpty();
        assertThat(userRepository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void existsByUsernameAndExistsByEmail_shouldReturnTrueAndFalseBranches() {
        userRepository.saveAndFlush(user("admin", "admin@example.com", UserRole.ADMIN));

        assertThat(userRepository.existsByUsername("admin")).isTrue();
        assertThat(userRepository.existsByUsername("missing")).isFalse();
        assertThat(userRepository.existsByEmail("admin@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    void saveAndFlush_shouldPersistRoleEnumAsStringValue() {
        User savedUser = userRepository.saveAndFlush(user("trainer", "trainer@example.com", UserRole.TRAINER));
        entityManager.clear();

        User result = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(result.getRole()).isEqualTo(UserRole.TRAINER);
    }

    @Test
    void saveAndFlush_shouldEnforceUniqueUsernameConstraint() {
        userRepository.saveAndFlush(user("johnny", "john@example.com", UserRole.USER));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("johnny", "other@example.com", UserRole.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveAndFlush_shouldEnforceUniqueEmailConstraint() {
        userRepository.saveAndFlush(user("johnny", "john@example.com", UserRole.USER));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("other", "john@example.com", UserRole.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static User user(String username, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }
}
