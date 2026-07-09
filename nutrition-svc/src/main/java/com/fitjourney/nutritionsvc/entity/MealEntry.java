package com.fitjourney.nutritionsvc.entity;

import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    private NutritionPlan nutritionPlan;

    @Column(name = "meal_name", nullable = false)
    private String mealName;

    @Column(nullable = false)
    private int calories;

    @Column(nullable = false)
    private int protein;

    @Column(nullable = false)
    private int carbs;

    @Column(nullable = false)
    private int fats;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;
}
