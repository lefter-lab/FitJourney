package com.fitjourney.nutritionsvc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "meal_entries")
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    private NutritionPlan nutritionPlan;

    @NotBlank
    @Column(name = "meal_name", nullable = false)
    private String mealName;

    @Min(1)
    @Column(nullable = false)
    private int calories;

    @Min(0)
    @Column(nullable = false)
    private int protein;

    @Min(0)
    @Column(nullable = false)
    private int carbs;

    @Min(0)
    @Column(nullable = false)
    private int fats;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;
}
