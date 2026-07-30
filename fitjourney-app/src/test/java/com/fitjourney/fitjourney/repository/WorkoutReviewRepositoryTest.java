package com.fitjourney.fitjourney.repository;

import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.entity.WorkoutReview;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@ActiveProfiles("test")
class WorkoutReviewRepositoryTest {

    @Autowired
    private WorkoutReviewRepository workoutReviewRepository;

    @Autowired
    private WorkoutProgramRepository workoutProgramRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByWorkoutProgramId_shouldReturnReviewsForProgram() {
        User reviewer = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        WorkoutProgram program = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        WorkoutReview savedReview = workoutReviewRepository.saveAndFlush(
                workoutReview(reviewer, program, 5, "Excellent program")
        );

        List<WorkoutReview> result = workoutReviewRepository.findAllByWorkoutProgramId(program.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(savedReview.getId());
        assertThat(result.get(0).getWorkoutProgram().getId()).isEqualTo(program.getId());
        assertThat(result.get(0).getUser().getId()).isEqualTo(reviewer.getId());
        assertThat(result.get(0).getRating()).isEqualTo(5);
        assertThat(result.get(0).getComment()).isEqualTo("Excellent program");
        assertThat(result.get(0).getCreatedAt()).isEqualTo(savedReview.getCreatedAt());
    }

    @Test
    void findAllByWorkoutProgramId_shouldReturnEmptyListWhenProgramHasNoReviews() {
        WorkoutProgram program = workoutProgramRepository.saveAndFlush(
                workoutProgram(trainer("trainer", "trainer@example.com"))
        );

        List<WorkoutReview> result = workoutReviewRepository.findAllByWorkoutProgramId(program.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByWorkoutProgramId_shouldIsolateReviewsBetweenPrograms() {
        User reviewer = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        WorkoutProgram firstProgram = workoutProgramRepository.save(workoutProgram(trainer("first-trainer", "first@example.com")));
        WorkoutProgram secondProgram = workoutProgramRepository.save(workoutProgram(trainer("second-trainer", "second@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        workoutReviewRepository.save(workoutReview(reviewer, firstProgram, 5, "Excellent program"));
        workoutReviewRepository.save(workoutReview(reviewer, secondProgram, 3, "Good program"));
        workoutReviewRepository.flush();

        List<WorkoutReview> result = workoutReviewRepository.findAllByWorkoutProgramId(firstProgram.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWorkoutProgram().getId()).isEqualTo(firstProgram.getId());
        assertThat(result.get(0).getComment()).isEqualTo("Excellent program");
    }

    @Test
    void existsByUserIdAndWorkoutProgramId_shouldReturnTrueAndFalseBranches() {
        User reviewer = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        User otherReviewer = userRepository.save(user("other", "other@example.com", UserRole.USER));
        WorkoutProgram program = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        workoutReviewRepository.saveAndFlush(workoutReview(reviewer, program, 5, "Excellent program"));

        assertThat(workoutReviewRepository.existsByUserIdAndWorkoutProgramId(reviewer.getId(), program.getId())).isTrue();
        assertThat(workoutReviewRepository.existsByUserIdAndWorkoutProgramId(otherReviewer.getId(), program.getId())).isFalse();
    }

    @Test
    void saveAndFindById_shouldPersistRatingCommentCreatedAtUserAndWorkoutProgramRelations() {
        User reviewer = userRepository.save(user("johnny", "john@example.com", UserRole.USER));
        WorkoutProgram program = workoutProgramRepository.save(workoutProgram(trainer("trainer", "trainer@example.com")));
        userRepository.flush();
        workoutProgramRepository.flush();
        WorkoutReview savedReview = workoutReviewRepository.saveAndFlush(
                workoutReview(reviewer, program, 4, "Very good program")
        );
        entityManager.clear();

        WorkoutReview result = workoutReviewRepository.findById(savedReview.getId()).orElseThrow();

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Very good program");
        assertThat(result.getCreatedAt())
                .isCloseTo(savedReview.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(result.getUser().getId()).isEqualTo(reviewer.getId());
        assertThat(result.getWorkoutProgram().getId()).isEqualTo(program.getId());
    }

    @Test
    void saveAndFlush_shouldRejectReviewWithoutUser() {
        WorkoutProgram program = workoutProgramRepository.saveAndFlush(
                workoutProgram(trainer("trainer", "trainer@example.com"))
        );
        WorkoutReview review = workoutReview(null, program, 5, "Excellent program");

        assertThatThrownBy(() -> workoutReviewRepository.saveAndFlush(review))
                .isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
    }

    @Test
    void saveAndFlush_shouldRejectReviewWithoutWorkoutProgram() {
        User reviewer = userRepository.saveAndFlush(user("johnny", "john@example.com", UserRole.USER));
        WorkoutReview review = workoutReview(reviewer, null, 5, "Excellent program");

        assertThatThrownBy(() -> workoutReviewRepository.saveAndFlush(review))
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

    private static WorkoutReview workoutReview(User user, WorkoutProgram program, int rating, String comment) {
        WorkoutReview review = new WorkoutReview();
        review.setUser(user);
        review.setWorkoutProgram(program);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }
}
