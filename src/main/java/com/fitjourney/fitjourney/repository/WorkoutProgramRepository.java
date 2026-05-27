package com.fitjourney.fitjourney.repository;

import com.fitjourney.fitjourney.entity.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutProgramRepository extends JpaRepository<WorkoutProgram, UUID> {

    List<WorkoutProgram> findAllByActiveTrue();

    List<WorkoutProgram> findAllByTrainerId(UUID trainerId);
}

