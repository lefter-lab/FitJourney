package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.exception.ProgramNotFoundException;
import com.fitjourney.fitjourney.exception.UnauthorizedProgramAccessException;
import com.fitjourney.fitjourney.repository.WorkoutProgramRepository;
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
class WorkoutProgramServiceTest {

    @Mock
    private WorkoutProgramRepository workoutProgramRepository;

    @InjectMocks
    private WorkoutProgramService workoutProgramService;

    @Test
    void createProgram_shouldMapDtoAndSaveProgram() {
        WorkoutProgramDto dto = new WorkoutProgramDto();
        dto.setName("Strength Builder");
        dto.setDescription("A structured strength training program.");
        dto.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        dto.setDurationWeeks(12);
        dto.setPrice(149.99);

        User trainer = new User();
        UUID trainerId = UUID.randomUUID();
        trainer.setId(trainerId);
        trainer.setUsername("trainer1");

        workoutProgramService.createProgram(dto, trainer);

        ArgumentCaptor<WorkoutProgram> captor = ArgumentCaptor.forClass(WorkoutProgram.class);
        verify(workoutProgramRepository, times(1)).save(captor.capture());

        WorkoutProgram savedProgram = captor.getValue();
        assertThat(savedProgram.getTitle()).isEqualTo(dto.getName());
        assertThat(savedProgram.getDescription()).isEqualTo(dto.getDescription());
        assertThat(savedProgram.getDifficulty()).isEqualTo(dto.getDifficultyLevel());
        assertThat(savedProgram.getDurationWeeks()).isEqualTo(dto.getDurationWeeks());
        assertThat(savedProgram.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(dto.getPrice()));
        assertThat(savedProgram.getTrainer()).isSameAs(trainer);
        assertThat(savedProgram.isActive()).isTrue();
    }

    @Test
    void getAllPrograms_shouldReturnOnlyActiveProgramsFromRepository() {
        WorkoutProgram firstProgram = workoutProgram(UUID.randomUUID(), "Program One");
        WorkoutProgram secondProgram = workoutProgram(UUID.randomUUID(), "Program Two");
        List<WorkoutProgram> activePrograms = List.of(firstProgram, secondProgram);
        when(workoutProgramRepository.findAllByActiveTrue()).thenReturn(activePrograms);

        List<WorkoutProgram> result = workoutProgramService.getAllPrograms();

        assertThat(result).containsExactly(firstProgram, secondProgram);
        verify(workoutProgramRepository, times(1)).findAllByActiveTrue();
    }

    @Test
    void findById_shouldReturnProgramWhenFound() {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId, "Program One");
        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));

        WorkoutProgram result = workoutProgramService.findById(programId);

        assertThat(result).isSameAs(program);
    }

    @Test
    void findById_shouldThrowProgramNotFoundExceptionWhenMissing() {
        UUID programId = UUID.randomUUID();
        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutProgramService.findById(programId))
                .isInstanceOf(ProgramNotFoundException.class)
                .hasMessageContaining(programId.toString());
    }

    @Test
    void updateProgram_shouldUpdateAndSaveWhenTrainerOwnsProgram() {
        UUID programId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();

        User trainer = user(trainerId, "trainer");
        WorkoutProgram program = workoutProgram(programId, "Original Program");
        program.setDescription("Original description");
        program.setDifficulty(DifficultyLevel.BEGINNER);
        program.setDurationWeeks(8);
        program.setPrice(BigDecimal.valueOf(79.99));
        program.setTrainer(user(trainerId, "trainer"));

        WorkoutProgramDto dto = new WorkoutProgramDto();
        dto.setName("Updated Program");
        dto.setDescription("Updated structured description.");
        dto.setDifficultyLevel(DifficultyLevel.ADVANCED);
        dto.setDurationWeeks(16);
        dto.setPrice(199.99);

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));

        workoutProgramService.updateProgram(programId, dto, trainer);

        ArgumentCaptor<WorkoutProgram> captor = ArgumentCaptor.forClass(WorkoutProgram.class);
        verify(workoutProgramRepository, times(1)).save(captor.capture());

        WorkoutProgram savedProgram = captor.getValue();
        assertThat(savedProgram.getTitle()).isEqualTo(dto.getName());
        assertThat(savedProgram.getDescription()).isEqualTo(dto.getDescription());
        assertThat(savedProgram.getDifficulty()).isEqualTo(dto.getDifficultyLevel());
        assertThat(savedProgram.getDurationWeeks()).isEqualTo(dto.getDurationWeeks());
        assertThat(savedProgram.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(dto.getPrice()));
    }

    @Test
    void updateProgram_shouldThrowWhenTrainerDoesNotOwnProgram() {
        UUID programId = UUID.randomUUID();
        UUID programTrainerId = UUID.randomUUID();
        UUID differentTrainerId = UUID.randomUUID();

        WorkoutProgram program = workoutProgram(programId, "Program");
        program.setTrainer(user(programTrainerId, "owner"));

        WorkoutProgramDto dto = new WorkoutProgramDto();
        dto.setName("Updated");
        dto.setDescription("Updated description");
        dto.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        dto.setDurationWeeks(10);
        dto.setPrice(109.99);

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));

        User differentTrainer = user(differentTrainerId, "other");

        assertThatThrownBy(() -> workoutProgramService.updateProgram(programId, dto, differentTrainer))
                .isInstanceOf(UnauthorizedProgramAccessException.class);

        verify(workoutProgramRepository, never()).save(org.mockito.ArgumentMatchers.any(WorkoutProgram.class));
    }

    @Test
    void deactivateProgram_shouldSetActiveFalseAndSave() {
        UUID programId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();

        User trainer = user(trainerId, "trainer");
        WorkoutProgram program = workoutProgram(programId, "Program");
        program.setTrainer(user(trainerId, "trainer"));
        program.setActive(true);

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));

        workoutProgramService.deactivateProgram(programId, trainer);

        ArgumentCaptor<WorkoutProgram> captor = ArgumentCaptor.forClass(WorkoutProgram.class);
        verify(workoutProgramRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void deleteProgram_shouldDeleteWhenTrainerOwnsProgram() {
        UUID programId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();

        User trainer = user(trainerId, "trainer");
        WorkoutProgram program = workoutProgram(programId, "Program");
        program.setTrainer(user(trainerId, "trainer"));

        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));

        workoutProgramService.deleteProgram(programId, trainer);

        verify(workoutProgramRepository, times(1)).delete(program);
    }

    @Test
    void deleteProgram_shouldThrowWhenTrainerDoesNotOwnProgram() {
        UUID programId = UUID.randomUUID();
        UUID programTrainerId = UUID.randomUUID();
        UUID differentTrainerId = UUID.randomUUID();

        WorkoutProgram program = workoutProgram(programId, "Program");
        program.setTrainer(user(programTrainerId, "owner"));
        when(workoutProgramRepository.findById(programId)).thenReturn(Optional.of(program));

        User differentTrainer = user(differentTrainerId, "other");

        assertThatThrownBy(() -> workoutProgramService.deleteProgram(programId, differentTrainer))
                .isInstanceOf(UnauthorizedProgramAccessException.class);

        verify(workoutProgramRepository, never()).delete(org.mockito.ArgumentMatchers.any(WorkoutProgram.class));
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
        return program;
    }

    private static User user(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
