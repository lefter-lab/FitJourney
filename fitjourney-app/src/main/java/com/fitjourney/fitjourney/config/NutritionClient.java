package com.fitjourney.fitjourney.config;

import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "nutrition-svc", url = "http://localhost:8081/nutrition")
public interface NutritionClient {

    @GetMapping("/programs/{programId}")
    Object getPlanByProgramId(@PathVariable("programId") UUID programId);
}