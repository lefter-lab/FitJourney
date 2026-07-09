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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    // GET /nutrition/programs/{programId}
    // Извиква се от main app когато потребител отвори страницата на програмата
    @GetMapping("/programs/{programId}")
    public ResponseEntity<NutritionPlanResponseDto> getPlanByProgram(@PathVariable UUID programId) {
        NutritionPlanResponseDto plan = nutritionService.getPlanByProgramId(programId);
        return ResponseEntity.ok(plan);
    }

    // POST /nutrition/plans
    // Извиква се от main app когато TRAINER създаде програма
    @PostMapping("/plans")
    public ResponseEntity<NutritionPlanResponseDto> createPlan(@Valid @RequestBody NutritionPlanDto dto) {
        NutritionPlanResponseDto created = nutritionService.createPlan(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // POST /nutrition/plans/{id}/meals
    // Извиква се от main app когато TRAINER добави хранене към план
    @PostMapping("/plans/{id}/meals")
    public ResponseEntity<MealEntryResponseDto> addMeal(
            @PathVariable UUID id,
            @Valid @RequestBody MealEntryDto dto) {
        MealEntryResponseDto meal = nutritionService.addMeal(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(meal);
    }

    // DELETE /nutrition/plans/{id}
    // Извиква се от main app когато TRAINER изтрие програма
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {
        nutritionService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
