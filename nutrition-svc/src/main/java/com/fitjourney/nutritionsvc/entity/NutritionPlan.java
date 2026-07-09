package com.fitjourney.nutritionsvc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "nutrition_plans")
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Reference to WorkoutProgram in main app (no direct FK — microservice boundary)
    @Column(name = "program_id", nullable = false, unique = true)
    private UUID programId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "daily_calories", nullable = false)
    private int dailyCalories;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
