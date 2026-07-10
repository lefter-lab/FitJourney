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

    @NotNull(message = "Calories are required")
    @Min(value = 1, message = "Calories must be positive")
    private Integer calories;

    @NotNull(message = "Protein is required")
    @Min(value = 0, message = "Protein cannot be negative")
    private Integer protein;

    @NotNull(message = "Carbs are required")
    @Min(value = 0, message = "Carbs cannot be negative")
    private Integer carbs;

    @NotNull(message = "Fats are required")
    @Min(value = 0, message = "Fats cannot be negative")
    private Integer fats;

    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;
}
