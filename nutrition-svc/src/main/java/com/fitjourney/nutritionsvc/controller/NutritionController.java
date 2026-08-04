package com.fitjourney.nutritionsvc.controller;

import com.fitjourney.nutritionsvc.dto.MealEntryDto;
import com.fitjourney.nutritionsvc.dto.MealEntryResponseDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanResponseDto;
import com.fitjourney.nutritionsvc.service.NutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    @PostMapping("/programs")
    public ResponseEntity<NutritionPlanResponseDto> createPlan(@Valid @RequestBody NutritionPlanDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nutritionService.createPlan(dto));
    }

    @GetMapping("/programs/{programId}")
    public ResponseEntity<NutritionPlanResponseDto> getPlanByProgramId(@PathVariable UUID programId) {
        return ResponseEntity.ok(nutritionService.getPlanByProgramId(programId));
    }

    @PostMapping("/plans/{planId}/meals")
    public ResponseEntity<MealEntryResponseDto> addMealEntry(@PathVariable UUID planId, @Valid @RequestBody MealEntryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nutritionService.addMealEntry(planId, dto));
    }
}
