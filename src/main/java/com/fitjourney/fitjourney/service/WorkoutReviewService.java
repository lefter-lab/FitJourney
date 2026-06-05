package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.WorkoutReviewDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.entity.WorkoutReview;
import com.fitjourney.fitjourney.repository.WorkoutReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutReviewService {

    private final WorkoutReviewRepository workoutReviewRepository;

    public void createReview(User user, WorkoutProgram program, WorkoutReviewDto dto) {
        boolean alreadyReviewed = workoutReviewRepository.existsByUserIdAndWorkoutProgramId(
                user.getId(),
                program.getId()
        );

        if (alreadyReviewed) {
            throw new IllegalArgumentException("You have already reviewed this program");
        }

        WorkoutReview review = new WorkoutReview();
        review.setUser(user);
        review.setWorkoutProgram(program);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        workoutReviewRepository.save(review);
    }

    public List<WorkoutReview> getReviewsForProgram(UUID programId) {
        return workoutReviewRepository.findAllByWorkoutProgramId(programId);
    }
}