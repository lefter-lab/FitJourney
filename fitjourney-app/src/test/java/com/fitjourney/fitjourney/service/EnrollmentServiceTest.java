package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.enums.EnrollmentStatus;
import com.fitjourney.fitjourney.exception.DuplicateEnrollmentException;
import com.fitjourney.fitjourney.exception.EnrollmentNotFoundException;
import com.fitjourney.fitjourney.repository.EnrollmentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void enrollUser_shouldSaveActiveEnrollmentForNewUser() {
        User user = user(UUID.randomUUID(), "john");
        WorkoutProgram program = program(UUID.randomUUID(), "Strength 101");
        when(enrollmentRepository.existsByUserIdAndWorkoutProgramId(user.getId(), program.getId())).thenReturn(false);

        enrollmentService.enrollUser(user, program);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, times(1)).save(captor.capture());

        Enrollment savedEnrollment = captor.getValue();
        assertThat(savedEnrollment.getUser()).isSameAs(user);
        assertThat(savedEnrollment.getWorkoutProgram()).isSameAs(program);
        assertThat(savedEnrollment.getEnrolledAt()).isNotNull();
        assertThat(savedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(savedEnrollment.getProgressPercentage()).isZero();
    }

    @Test
    void enrollUser_shouldThrowDuplicateEnrollmentExceptionWhenAlreadyEnrolled() {
        User user = user(UUID.randomUUID(), "john");
        WorkoutProgram program = program(UUID.randomUUID(), "Strength 101");
        when(enrollmentRepository.existsByUserIdAndWorkoutProgramId(user.getId(), program.getId())).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enrollUser(user, program))
                .isInstanceOf(DuplicateEnrollmentException.class)
                .hasMessage("You are already enrolled in this program");

        verify(enrollmentRepository, never()).save(org.mockito.ArgumentMatchers.any(Enrollment.class));
    }

    @Test
    void getEnrollmentsByUser_shouldReturnEnrollmentsForUser() {
        User user = user(UUID.randomUUID(), "john");
        Enrollment firstEnrollment = enrollment(UUID.randomUUID(), user, program(UUID.randomUUID(), "Program One"));
        Enrollment secondEnrollment = enrollment(UUID.randomUUID(), user, program(UUID.randomUUID(), "Program Two"));
        when(enrollmentRepository.findAllByUserId(user.getId())).thenReturn(List.of(firstEnrollment, secondEnrollment));

        List<Enrollment> result = enrollmentService.getEnrollmentsByUser(user);

        assertThat(result).containsExactly(firstEnrollment, secondEnrollment);
        verify(enrollmentRepository, times(1)).findAllByUserId(user.getId());
    }

    @Test
    void updateProgress_shouldUpdateAndSaveEnrollment() {
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = enrollment(UUID.randomUUID(), user(UUID.randomUUID(), "john"), program(UUID.randomUUID(), "Program"));
        enrollment.setId(enrollmentId);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        enrollmentService.updateProgress(enrollmentId, 75);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, times(1)).save(captor.capture());

        Enrollment savedEnrollment = captor.getValue();
        assertThat(savedEnrollment.getProgressPercentage()).isEqualTo(75);
    }

    @Test
    void updateProgress_shouldThrowEnrollmentNotFoundExceptionWhenMissing() {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.updateProgress(enrollmentId, 75))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Enrollment not found");

        verify(enrollmentRepository, never()).save(org.mockito.ArgumentMatchers.any(Enrollment.class));
    }

    private static Enrollment enrollment(UUID id, User user, WorkoutProgram program) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(id);
        enrollment.setUser(user);
        enrollment.setWorkoutProgram(program);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercentage(0);
        return enrollment;
    }

    private static User user(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static WorkoutProgram program(UUID id, String title) {
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
