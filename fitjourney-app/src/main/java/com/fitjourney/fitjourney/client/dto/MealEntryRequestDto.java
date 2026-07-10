package com.fitjourney.fitjourney.client.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealEntryRequestDto {

    @NotBlank(message = "Meal name is required")
    @Size(min = 2, max = 100, message = "Meal name must be between 2 and 100 characters")
    private String mealName;

    @NotBlank(message = "Meal time is required")
    @Size(max = 50, message = "Meal time cannot exceed 50 characters")
    private String mealTime;

    @NotNull(message = "Calories are required")
    @Min(value = 1, message = "Calories must be positive")
    private Integer calories;
}
