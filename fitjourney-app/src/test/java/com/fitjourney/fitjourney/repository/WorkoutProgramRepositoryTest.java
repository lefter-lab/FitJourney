package com.fitjourney.fitjourney.repository;

import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.enums.UserRole;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class WorkoutProgramRepositoryTest {

    @Autowired
    private WorkoutProgramRepository workoutProgramRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindById_shouldPersistProgramFieldsTrainerRelationAndDifficultyEnum() {
        User trainer = userRepository.saveAndFlush(user("trainer", "trainer@example.com", UserRole.TRAINER));
        WorkoutProgram savedProgram = workoutProgramRepository.saveAndFlush(
                workoutProgram(trainer, "Strength Plan", DifficultyLevel.INTERMEDIATE, true)
        );
        entityManager.clear();

        WorkoutProgram result = workoutProgramRepository.findById(savedProgram.getId()).orElseThrow();

        assertThat(result.getId()).isEqualTo(savedProgram.getId());
        assertThat(result.getTitle()).isEqualTo("Strength Plan");
        assertThat(result.getDescription()).isEqualTo("Complete strength program");
        assertThat(result.getDifficulty()).isEqualTo(DifficultyLevel.INTERMEDIATE);
        assertThat(result.getDurationWeeks()).isEqualTo(8);
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getTrainer().getId()).isEqualTo(trainer.getId());
    }

    @Test
    void findAllByActiveTrue_shouldReturnOnlyActivePrograms() {
        User trainer = userRepository.saveAndFlush(user("trainer", "trainer@example.com", UserRole.TRAINER));
        workoutProgramRepository.save(workoutProgram(trainer, "Active Plan", DifficultyLevel.BEGINNER, true));
        workoutProgramRepository.save(workoutProgram(trainer, "Inactive Plan", DifficultyLevel.ADVANCED, false));
        workoutProgramRepository.flush();

        List<WorkoutProgram> result = workoutProgramRepository.findAllByActiveTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Active Plan");
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void saveAndFlush_shouldRejectProgramWithoutTrainer() {
        WorkoutProgram program = workoutProgram(null, "Strength Plan", DifficultyLevel.INTERMEDIATE, true);

        assertThatThrownBy(() -> workoutProgramRepository.saveAndFlush(program))
                .isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
    }

    private static User user(String username, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setFirstName("First");
        user.setLastName("Last");
        return user;
    }

    private static WorkoutProgram workoutProgram(User trainer, String title, DifficultyLevel difficulty, boolean active) {
        WorkoutProgram program = new WorkoutProgram();
        program.setTitle(title);
        program.setDescription("Complete strength program");
        program.setDifficulty(difficulty);
        program.setDurationWeeks(8);
        program.setPrice(BigDecimal.valueOf(49.99));
        program.setActive(active);
        program.setTrainer(trainer);
        return program;
    }
}
