package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.repository.WorkoutProgramRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutProgramService {

    private final WorkoutProgramRepository workoutProgramRepository;

    public void createProgram(WorkoutProgramDto dto, User creator) {
        WorkoutProgram program = new WorkoutProgram();
        program.setTitle(dto.getName());
        program.setDescription(dto.getDescription());
        program.setDifficulty(dto.getDifficultyLevel());
        program.setDurationWeeks(dto.getDurationWeeks());
        program.setPrice(java.math.BigDecimal.valueOf(dto.getPrice()));
        program.setTrainer(creator);
        program.setActive(true);

        workoutProgramRepository.save(program);
    }

    public List<WorkoutProgram> getAllPrograms() {
        return workoutProgramRepository.findAllByActiveTrue();
    }

    public WorkoutProgram findById(UUID id) {
        return workoutProgramRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Program not found"));
    }

    public void updateProgram(UUID id, WorkoutProgramDto dto) {
        WorkoutProgram program = findById(id);
        program.setTitle(dto.getName());
        program.setDescription(dto.getDescription());
        program.setDifficulty(dto.getDifficultyLevel());
        program.setDurationWeeks(dto.getDurationWeeks());
        program.setPrice(BigDecimal.valueOf(dto.getPrice()));
        workoutProgramRepository.save(program);
    }

    public void deactivateProgram(UUID id) {
        WorkoutProgram program = findById(id);
        program.setActive(false);
        workoutProgramRepository.save(program);
    }
}
