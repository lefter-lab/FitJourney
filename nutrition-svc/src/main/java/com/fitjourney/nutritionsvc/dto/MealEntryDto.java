package com.fitjourney.nutritionsvc.dto;

import com.fitjourney.nutritionsvc.entity.DayOfWeek;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealEntryDto {

    @NotBlank(message = "Meal name is required")
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

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;
}
