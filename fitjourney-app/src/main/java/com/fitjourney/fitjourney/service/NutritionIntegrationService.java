package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.client.NutritionClient;
import com.fitjourney.fitjourney.client.dto.NutritionPlanRequestDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanResponseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionIntegrationService {

    private final NutritionClient nutritionClient;

    public Optional<NutritionPlanResponseDto> findPlanByProgramId(UUID programId) {
        try {
            return Optional.ofNullable(nutritionClient.getPlanByProgramId(programId));
        } catch (FeignException.NotFound exception) {
            log.warn("Nutrition plan not found for program ID: {}", programId);
            return Optional.empty();
        } catch (FeignException exception) {
            log.warn("Nutrition service is currently unavailable for program ID: {}", programId);
            return Optional.empty();
        } catch (RuntimeException exception) {
            log.warn("Nutrition service is currently unavailable for program ID: {}", programId, exception);
            return Optional.empty();
        }
    }

    public Optional<NutritionPlanResponseDto> createPlan(NutritionPlanRequestDto dto) {
        try {
            return Optional.ofNullable(nutritionClient.createPlan(dto));
        } catch (FeignException exception) {
            log.warn("Nutrition service failed to create plan for program ID: {}", dto.getProgramId());
            return Optional.empty();
        }
    }
}
