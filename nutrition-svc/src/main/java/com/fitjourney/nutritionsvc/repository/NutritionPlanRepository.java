package com.fitjourney.nutritionsvc.repository;

import com.fitjourney.nutritionsvc.entity.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, UUID> {
    Optional<NutritionPlan> findByProgramId(UUID programId);
    boolean existsByProgramId(UUID programId);
}