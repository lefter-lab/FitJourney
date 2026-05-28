package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.repository.WorkoutReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutReviewService {

    private final WorkoutReviewRepository workoutReviewRepository;
}

