package com.fitjourney.nutritionsvc.service;

import com.fitjourney.nutritionsvc.dto.*;
import com.fitjourney.nutritionsvc.entity.*;
import com.fitjourney.nutritionsvc.exception.*;
import com.fitjourney.nutritionsvc.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final MealEntryRepository mealEntryRepository;

    @Transactional
    public NutritionPlanResponseDto createPlan(NutritionPlanDto dto) {
        log.info("Attempting to create nutrition plan for programId: {}", dto.getProgramId());
        
        if (nutritionPlanRepository.existsByProgramId(dto.getProgramId())) {
            log.warn("Nutrition plan already exists for programId: {}", dto.getProgramId());
            throw new DuplicatePlanException("A nutrition plan already exists for this workout program.");
        }

        NutritionPlan plan = new NutritionPlan();
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setDailyCalories(dto.getDailyCalories());
        plan.setProgramId(dto.getProgramId());
        plan.setCreatedAt(LocalDateTime.now());

        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        return mapToPlanResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public NutritionPlanResponseDto getPlanByProgramId(UUID programId) {
        NutritionPlan plan = nutritionPlanRepository.findByProgramId(programId)
                .orElseThrow(() -> new NutritionPlanNotFoundException("Nutrition plan not found for program ID: " + programId));
        return mapToPlanResponse(plan);
    }

    @Transactional
    public MealEntryResponseDto addMealEntry(UUID planId, MealEntryDto dto) {
        NutritionPlan plan = nutritionPlanRepository.findById(planId)
                .orElseThrow(() -> new NutritionPlanNotFoundException("Nutrition plan not found with ID: " + planId));

        MealEntry entry = new MealEntry();
        entry.setMealName(dto.getMealName());
        entry.setCalories(dto.getCalories());
        entry.setProtein(dto.getProtein());
        entry.setCarbs(dto.getCarbs());
        entry.setFats(dto.getFats());
        
       
        entry.setDayOfWeek(dto.getDayOfWeek());
        entry.setNutritionPlan(plan);

        MealEntry savedEntry = mealEntryRepository.save(entry);
        return mapToMealResponse(savedEntry);
    }

    private NutritionPlanResponseDto mapToPlanResponse(NutritionPlan plan) {
        NutritionPlanResponseDto response = new NutritionPlanResponseDto();
        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setDescription(plan.getDescription());
        response.setDailyCalories(plan.getDailyCalories());
        response.setProgramId(plan.getProgramId());
        return response;
    }

    private MealEntryResponseDto mapToMealResponse(MealEntry entry) {
        MealEntryResponseDto response = new MealEntryResponseDto();
        response.setId(entry.getId());
        response.setMealName(entry.getMealName());
        response.setCalories(entry.getCalories());
        response.setProtein(entry.getProtein());
        response.setCarbs(entry.getCarbs());
        response.setFats(entry.getFats());
        response.setDayOfWeek(entry.getDayOfWeek());
        return response;
    }
}