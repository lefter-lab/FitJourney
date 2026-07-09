package com.fitjourney.nutritionsvc.dto;

import com.fitjourney.nutritionsvc.entity.DayOfWeek;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MealEntryResponseDto {

    private UUID id;
    private String mealName;
    private int calories;
    private int protein;
    private int carbs;
    private int fats;
    private DayOfWeek dayOfWeek;
}
