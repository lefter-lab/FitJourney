package com.fitjourney.fitjourney.client.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NutritionPlanRequestDto {

    @NotNull(message = "Program ID is required")
    private UUID programId;

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Daily calories are required")
    @Min(value = 1, message = "Daily calories must be positive")
    private Integer dailyCalories;
}
