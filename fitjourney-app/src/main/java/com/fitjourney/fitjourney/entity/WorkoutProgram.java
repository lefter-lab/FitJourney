package com.fitjourney.fitjourney.entity;

import com.fitjourney.fitjourney.enums.DifficultyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "workout_programs")
public class WorkoutProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 5, max = 50)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Size(min = 10)
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Min(1)
    @Column(name = "duration_weeks", nullable = false)
    private int durationWeeks;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;
}

