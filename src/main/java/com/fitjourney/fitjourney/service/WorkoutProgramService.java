package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.repository.WorkoutProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutProgramService {

    private final WorkoutProgramRepository workoutProgramRepository;
}

