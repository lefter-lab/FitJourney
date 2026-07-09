package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.WorkoutReviewDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.entity.WorkoutReview;
import com.fitjourney.fitjourney.exception.DuplicateReviewException;
import com.fitjourney.fitjourney.exception.ProgramNotFoundException;
import com.fitjourney.fitjourney.repository.WorkoutProgramRepository;
import com.fitjourney.fitjourney.repository.WorkoutReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutReviewService {

    private final WorkoutReviewRepository workoutReviewRepository;
    private final WorkoutProgramRepository workoutProgramRepository;

    public void addReview(WorkoutReviewDto dto, User user) {
        WorkoutProgram program = workoutProgramRepository.findById(dto.getProgramId())
                .orElseThrow(() -> new ProgramNotFoundException("Program not found"));

        boolean alreadyReviewed = workoutReviewRepository.existsByUserIdAndWorkoutProgramId(
                user.getId(),
                program.getId()
        );

        if (alreadyReviewed) {
            throw new DuplicateReviewException("You have already reviewed this program");
        }

        WorkoutReview review = new WorkoutReview();
        review.setUser(user);
        review.setWorkoutProgram(program);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        workoutReviewRepository.save(review);
        log.info("New review with rating {} added for program ID '{}'.", dto.getRating(), program.getId());
    }

    public List<WorkoutReview> getReviewsForProgram(UUID programId) {
        return workoutReviewRepository.findAllByWorkoutProgramId(programId);
    }
}