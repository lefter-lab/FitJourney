package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.repository.WorkoutProgramRepository;
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
}

