package com.fitjourney.fitjourney.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NutritionPlanResponseDto {

    private UUID id;
    private UUID programId;
    private String name;
    private String description;
    private Integer dailyCalories;
}
