package com.fitjourney.fitjourney.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MealEntryResponseDto {

    private UUID id;
    private String mealName;
    private String mealTime;
    private Integer calories;
}
