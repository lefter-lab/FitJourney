package com.fitjourney.nutritionsvc.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NutritionPlanResponseDto {

    private UUID id;
    private UUID programId;
    private String name;
    private String description;
    private int dailyCalories;
    private List<MealEntryResponseDto> meals;
}
