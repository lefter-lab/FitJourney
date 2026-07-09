package com.fitjourney.fitjourney.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentProgressDto {

    @NotNull(message = "Progress is required")
    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must be at most 100")
    private Integer percentage;
}
