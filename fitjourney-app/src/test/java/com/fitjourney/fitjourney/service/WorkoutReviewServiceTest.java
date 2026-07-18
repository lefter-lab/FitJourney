package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.WorkoutReviewDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.entity.WorkoutReview;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.exception.DuplicateReviewException;
import com.fitjourney.fitjourney.exception.ProgramNotFoundException;
import com.fitjourney.fitjourney.repository.WorkoutProgramRepository;
import com.fitjourney.fitjourney.repository.WorkoutReviewRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutReviewServiceTest {

    @Mock
    private WorkoutReviewRepository workoutReviewRepository;

    @Mock
    private WorkoutProgramRepository workoutProgramRepository;

    @InjectMocks
    private WorkoutReviewService workoutReviewService;

    @Test
    void addReview_shouldSaveReviewWhenProgramExistsAndUserHasNotReviewed() {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "john");
        WorkoutProgram program = workoutProgram(programId, "Strength Builder");
        WorkoutReviewDto dto = reviewDto(programId, 5, "Excellent program.");

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));
        when(workoutReviewRepository.existsByUserIdAndWorkoutProgramId(user.getId(), program.getId())).thenReturn(false);

        workoutReviewService.addReview(dto, user);

        ArgumentCaptor<WorkoutReview> captor = ArgumentCaptor.forClass(WorkoutReview.class);
        verify(workoutReviewRepository, times(1)).save(captor.capture());

        WorkoutReview savedReview = captor.getValue();
        assertThat(savedReview.getUser()).isSameAs(user);
        assertThat(savedReview.getWorkoutProgram()).isSameAs(program);
        assertThat(savedReview.getRating()).isEqualTo(dto.getRating());
        assertThat(savedReview.getComment()).isEqualTo(dto.getComment());
        assertThat(savedReview.getCreatedAt()).isNotNull();
    }

    @Test
    void addReview_shouldThrowProgramNotFoundExceptionWhenProgramMissing() {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "john");
        WorkoutReviewDto dto = reviewDto(programId, 4, "Good program.");

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutReviewService.addReview(dto, user))
                .isInstanceOf(ProgramNotFoundException.class)
                .hasMessage("Program not found");

        verify(workoutReviewRepository, never()).save(org.mockito.ArgumentMatchers.any(WorkoutReview.class));
    }

    @Test
    void addReview_shouldThrowDuplicateReviewExceptionWhenUserAlreadyReviewedProgram() {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "john");
        WorkoutProgram program = workoutProgram(programId, "Strength Builder");
        WorkoutReviewDto dto = reviewDto(programId, 5, "Excellent program.");

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));
        when(workoutReviewRepository.existsByUserIdAndWorkoutProgramId(user.getId(), program.getId())).thenReturn(true);

        assertThatThrownBy(() -> workoutReviewService.addReview(dto, user))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessage("You have already reviewed this program");

        verify(workoutReviewRepository, never()).save(org.mockito.ArgumentMatchers.any(WorkoutReview.class));
    }

    @Test
    void getReviewsForProgram_shouldReturnReviewsFromRepository() {
        UUID programId = UUID.randomUUID();
        WorkoutReview firstReview = review(UUID.randomUUID(), 5, "Excellent program.");
        WorkoutReview secondReview = review(UUID.randomUUID(), 4, "Very useful.");
        when(workoutReviewRepository.findAllByWorkoutProgramId(programId)).thenReturn(List.of(firstReview, secondReview));

        List<WorkoutReview> result = workoutReviewService.getReviewsForProgram(programId);

        assertThat(result).containsExactly(firstReview, secondReview);
        verify(workoutReviewRepository, times(1)).findAllByWorkoutProgramId(programId);
    }

    private static WorkoutReviewDto reviewDto(UUID programId, Integer rating, String comment) {
        WorkoutReviewDto dto = new WorkoutReviewDto();
        dto.setProgramId(programId);
        dto.setRating(rating);
        dto.setComment(comment);
        return dto;
    }

    private static WorkoutReview review(UUID id, int rating, String comment) {
        WorkoutReview review = new WorkoutReview();
        review.setId(id);
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }

    private static User user(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static WorkoutProgram workoutProgram(UUID id, String title) {
        WorkoutProgram program = new WorkoutProgram();
        program.setId(id);
        program.setTitle(title);
        program.setDescription("Description for " + title);
        program.setDifficulty(DifficultyLevel.BEGINNER);
        program.setDurationWeeks(8);
        program.setPrice(BigDecimal.valueOf(99.99));
        program.setActive(true);
        program.setTrainer(user(UUID.randomUUID(), "trainer"));
        return program;
    }
}
