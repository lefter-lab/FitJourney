package com.fitjourney.fitjourney.repository;

import com.fitjourney.fitjourney.entity.WorkoutReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutReviewRepository extends JpaRepository<WorkoutReview, UUID> {

    List<WorkoutReview> findAllByWorkoutProgramId(UUID programId);

    boolean existsByUserIdAndWorkoutProgramId(UUID userId, UUID programId);
}

