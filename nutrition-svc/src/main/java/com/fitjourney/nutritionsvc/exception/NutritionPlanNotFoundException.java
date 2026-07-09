package com.fitjourney.nutritionsvc.exception;

public class NutritionPlanNotFoundException extends RuntimeException {

    public NutritionPlanNotFoundException(String message) {
        super(message);
    }
}
