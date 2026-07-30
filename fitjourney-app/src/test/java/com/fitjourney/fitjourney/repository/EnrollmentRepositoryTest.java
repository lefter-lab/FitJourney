package com.fitjourney.fitjourney.repository;

import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.enums.EnrollmentStatus;
import com.fitjourney.fitjourney.enums.UserRole;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@ActiveProfiles("test")
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutProgramRepository workoutProgramRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByUserId_shouldReturnEnrollmentsForUser() {
        User user = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        WorkoutProgram program = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        Enrollment savedEnrollment = enrollmentRepository.saveAndFlush(enrollment(user, program, EnrollmentStatus.ACTIVE, 45));

        List<Enrollment> result = enrollmentRepository.findAllByUserId(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(savedEnrollment.getId());
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get(0).getWorkoutProgram().getId()).isEqualTo(program.getId());
        assertThat(result.get(0).getProgressPercentage()).isEqualTo(45);
        assertThat(result.get(0).getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(result.get(0).getEnrolledAt()).isEqualTo(savedEnrollment.getEnrolledAt());
    }

    @Test
    void findAllByUserId_shouldIsolateEnrollmentsBetweenUsers() {
        User firstUser = userRepository.save(user("first-user", "first-user@example.com", UserRole.USER));
        User secondUser = userRepository.save(user("second-user", "second-user@example.com", UserRole.USER));
        WorkoutProgram program = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        enrollmentRepository.save(enrollment(firstUser, program, EnrollmentStatus.ACTIVE, 30));
        enrollmentRepository.save(enrollment(secondUser, program, EnrollmentStatus.ACTIVE, 60));
        enrollmentRepository.flush();

        List<Enrollment> result = enrollmentRepository.findAllByUserId(firstUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(firstUser.getId());
        assertThat(result.get(0).getProgressPercentage()).isEqualTo(30);
    }

    @Test
    void existsByUserIdAndWorkoutProgramId_shouldReturnTrueAndFalseBranches() {
        User user = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        WorkoutProgram firstProgram = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        WorkoutProgram secondProgram = workoutProgramRepository.save(workoutProgram(trainer("other-trainer", "other@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        enrollmentRepository.saveAndFlush(enrollment(user, firstProgram, EnrollmentStatus.ACTIVE, 30));

        assertThat(enrollmentRepository.existsByUserIdAndWorkoutProgramId(user.getId(), firstProgram.getId())).isTrue();
        assertThat(enrollmentRepository.existsByUserIdAndWorkoutProgramId(user.getId(), secondProgram.getId())).isFalse();
    }

    @Test
    void saveAndFindById_shouldPersistStatusProgressAndEnrollmentDate() {
        User user = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        WorkoutProgram program = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        Enrollment savedEnrollment = enrollmentRepository.saveAndFlush(enrollment(user, program, EnrollmentStatus.COMPLETED, 100));
        entityManager.clear();

        Enrollment result = enrollmentRepository.findById(savedEnrollment.getId()).orElseThrow();

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(result.getProgressPercentage()).isEqualTo(100);
        assertThat(result.getEnrolledAt())
                .isCloseTo(savedEnrollment.getEnrolledAt(), within(1, ChronoUnit.MICROS));
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
        assertThat(result.getWorkoutProgram().getId()).isEqualTo(program.getId());
    }

    @Test
    void saveAndFlush_shouldRejectEnrollmentWithoutUser() {
        WorkoutProgram program = workoutProgramRepository.saveAndFlush(workoutProgram(trainer("trainer", "trainer@example.com")));
        Enrollment enrollment = enrollment(null, program, EnrollmentStatus.ACTIVE, 0);

        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(enrollment))
                .isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
    }

    @Test
    void saveAndFlush_shouldRejectEnrollmentWithoutWorkoutProgram() {
        User user = userRepository.saveAndFlush(user("johnny", "john@example.com", UserRole.USER));
        Enrollment enrollment = enrollment(user, null, EnrollmentStatus.ACTIVE, 0);

        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(enrollment))
                .isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
    }

    private User trainer(String username, String email) {
        return userRepository.save(user(username, email, UserRole.TRAINER));
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

    private static WorkoutProgram workoutProgram(User trainer) {
        WorkoutProgram program = new WorkoutProgram();
        program.setTitle("Strength Plan");
        program.setDescription("Complete strength program");
        program.setDifficulty(DifficultyLevel.INTERMEDIATE);
        program.setDurationWeeks(8);
        program.setPrice(BigDecimal.valueOf(49.99));
        program.setActive(true);
        program.setTrainer(trainer);
        return program;
    }

    private static Enrollment enrollment(User user, WorkoutProgram program, EnrollmentStatus status, int progressPercentage) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setWorkoutProgram(program);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(status);
        enrollment.setProgressPercentage(progressPercentage);
        return enrollment;
    }
}
