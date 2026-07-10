package com.fitjourney.fitjourney.client;

import com.fitjourney.fitjourney.client.dto.NutritionPlanRequestDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "nutrition-svc", url = "${nutrition.service.url:http://localhost:8081/nutrition}")
public interface NutritionClient {

    @GetMapping("/programs/{programId}")
    NutritionPlanResponseDto getPlanByProgramId(@PathVariable("programId") UUID programId);

    @PostMapping("/programs")
    NutritionPlanResponseDto createPlan(@RequestBody NutritionPlanRequestDto dto);
}
