package com.fitjourney.nutritionsvc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    @Column(name = "program_id", nullable = false, unique = true)
    private UUID programId;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(1)
    @Column(name = "daily_calories", nullable = false)
    private int dailyCalories;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
