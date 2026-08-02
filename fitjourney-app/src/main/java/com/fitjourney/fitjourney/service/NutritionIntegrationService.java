package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.client.NutritionClient;
import com.fitjourney.fitjourney.client.dto.MealEntryRequestDto;
import com.fitjourney.fitjourney.client.dto.MealEntryResponseDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanRequestDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanResponseDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.UserRole;
import com.fitjourney.fitjourney.exception.UnauthorizedProgramAccessException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionIntegrationService {

    private final NutritionClient nutritionClient;
    private final UserService userService;
    private final WorkoutProgramService workoutProgramService;

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

    public Optional<NutritionPlanResponseDto> createPlan(UUID programId, NutritionPlanRequestDto dto, String username) {
        verifyNutritionManagementAccess(programId, username);
        dto.setProgramId(programId);

        try {
            NutritionPlanResponseDto responseDto = nutritionClient.createPlan(dto);
            if (responseDto != null) {
                log.info("Nutrition plan created successfully for program ID '{}'.", programId);
            }
            return Optional.ofNullable(responseDto);
        } catch (FeignException exception) {
            log.warn("Nutrition service failed to create plan for program ID: {}", programId);
            return Optional.empty();
        }
    }

    public Optional<MealEntryResponseDto> addMealToPlan(UUID programId, UUID planId, MealEntryRequestDto dto, String username) {
        verifyNutritionManagementAccess(programId, username);

        Optional<NutritionPlanResponseDto> nutritionPlan = findPlanForMealValidation(programId);
        if (nutritionPlan.isEmpty()) {
            return Optional.empty();
        }

        verifyPlanMatchesRoute(nutritionPlan.get(), programId, planId);

        try {
            MealEntryResponseDto responseDto = nutritionClient.addMealToPlan(planId, dto);
            if (responseDto != null) {
                log.info("Meal added successfully to nutrition plan ID '{}' for program ID '{}'.", planId, programId);
            }
            return Optional.ofNullable(responseDto);
        } catch (FeignException exception) {
            log.warn("Nutrition service failed to add meal to plan ID: {}", planId);
            return Optional.empty();
        }
    }

    private void verifyNutritionManagementAccess(UUID programId, String username) {
        WorkoutProgram program = workoutProgramService.findById(programId);
        User user = userService.findByUsername(username);

        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.TRAINER) {
            workoutProgramService.verifyTrainerOwnership(program, user);
            return;
        }

        throw new UnauthorizedProgramAccessException("You can only manage nutrition plans for your own workout programs");
    }

    private Optional<NutritionPlanResponseDto> findPlanForMealValidation(UUID programId) {
        try {
            return Optional.ofNullable(nutritionClient.getPlanByProgramId(programId));
        } catch (FeignException.NotFound exception) {
            log.warn("Nutrition plan not found for program ID: {}", programId);
            return Optional.empty();
        } catch (FeignException exception) {
            log.warn("Nutrition service is currently unavailable for program ID: {}", programId);
            return Optional.empty();
        }
    }

    private void verifyPlanMatchesRoute(NutritionPlanResponseDto nutritionPlan, UUID programId, UUID planId) {
        if (!planId.equals(nutritionPlan.getId()) || !programId.equals(nutritionPlan.getProgramId())) {
            throw new AccessDeniedException("Nutrition plan does not match the requested workout program");
        }
    }
}
