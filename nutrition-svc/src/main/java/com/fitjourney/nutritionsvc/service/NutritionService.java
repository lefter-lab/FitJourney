package com.fitjourney.nutritionsvc.service;

import com.fitjourney.nutritionsvc.dto.MealEntryDto;
import com.fitjourney.nutritionsvc.dto.MealEntryResponseDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanResponseDto;
import com.fitjourney.nutritionsvc.entity.MealEntry;
import com.fitjourney.nutritionsvc.entity.NutritionPlan;
import com.fitjourney.nutritionsvc.exception.DuplicatePlanException;
import com.fitjourney.nutritionsvc.exception.NutritionPlanNotFoundException;
import com.fitjourney.nutritionsvc.repository.MealEntryRepository;
import com.fitjourney.nutritionsvc.repository.NutritionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final MealEntryRepository mealEntryRepository;

    public NutritionPlanResponseDto getPlanByProgramId(UUID programId) {
        log.info("Fetching nutrition plan for program: {}", programId);

        // TODO: намери NutritionPlan по programId
        // TODO: намери всички MealEntry за плана
        // TODO: мапирай към NutritionPlanResponseDto и върни
        // Hint: хвърли NutritionPlanNotFoundException ако не съществува

        throw new UnsupportedOperationException("TODO: implement getPlanByProgramId");
    }

    public NutritionPlanResponseDto createPlan(NutritionPlanDto dto) {
        log.info("Creating nutrition plan for program: {}", dto.getProgramId());

        // TODO: провери дали вече съществува план за този programId
        // TODO: създай нов NutritionPlan от dto
        // TODO: запази и върни като NutritionPlanResponseDto
        // Hint: хвърли DuplicatePlanException ако вече има план

        throw new UnsupportedOperationException("TODO: implement createPlan");
    }

    public MealEntryResponseDto addMeal(UUID planId, MealEntryDto dto) {
        log.info("Adding meal to plan: {}", planId);

        // TODO: намери NutritionPlan по planId
        // TODO: създай нов MealEntry от dto
        // TODO: запази и върни като MealEntryResponseDto
        // Hint: хвърли NutritionPlanNotFoundException ако плана не съществува

        throw new UnsupportedOperationException("TODO: implement addMeal");
    }

    public void deletePlan(UUID planId) {
        log.info("Deleting nutrition plan: {}", planId);

        // TODO: намери NutritionPlan по planId
        // TODO: изтрий всички MealEntry за плана
        // TODO: изтрий плана
        // Hint: хвърли NutritionPlanNotFoundException ако не съществува

        throw new UnsupportedOperationException("TODO: implement deletePlan");
    }

    // Helper methods — напиши ги след като имплементираш горните методи
    private NutritionPlanResponseDto toResponseDto(NutritionPlan plan, List<MealEntry> meals) {
        // TODO: попълни mapping логиката
        throw new UnsupportedOperationException("TODO: implement toResponseDto");
    }

    private MealEntryResponseDto toMealResponseDto(MealEntry meal) {
        // TODO: попълни mapping логиката
        throw new UnsupportedOperationException("TODO: implement toMealResponseDto");
    }
}
