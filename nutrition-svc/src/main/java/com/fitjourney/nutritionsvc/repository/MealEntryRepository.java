package com.fitjourney.nutritionsvc.repository;

import com.fitjourney.nutritionsvc.entity.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, UUID> {

    List<MealEntry> findAllByNutritionPlanId(UUID nutritionPlanId);
}
