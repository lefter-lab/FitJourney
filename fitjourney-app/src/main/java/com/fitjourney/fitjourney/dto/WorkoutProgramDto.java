package com.fitjourney.fitjourney.dto;

import com.fitjourney.fitjourney.enums.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutProgramDto {

    @NotBlank
    @Size(min = 5, max = 50, message = "Name must be between 5 and 50 characters")
    private String name;

    @NotBlank
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotNull(message = "Please select a difficulty level")
    private DifficultyLevel difficultyLevel;

    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 week")
    private Integer durationWeeks;

    @NotNull
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
}

