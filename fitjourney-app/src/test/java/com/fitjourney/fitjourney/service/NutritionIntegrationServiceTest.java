package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.client.NutritionClient;
import com.fitjourney.fitjourney.client.dto.MealEntryRequestDto;
import com.fitjourney.fitjourney.client.dto.MealEntryResponseDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanRequestDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanResponseDto;
import feign.FeignException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionIntegrationServiceTest {

    @Mock
    private NutritionClient nutritionClient;

    @InjectMocks
    private NutritionIntegrationService nutritionIntegrationService;

    @Test
    void findPlanByProgramId_shouldReturnPlanWhenClientSucceeds() {
        UUID programId = UUID.randomUUID();
        NutritionPlanResponseDto responseDto = nutritionPlanResponseDto(UUID.randomUUID(), programId);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(responseDto);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.findPlanByProgramId(programId);

        assertThat(result).containsSame(responseDto);
        verify(nutritionClient, times(1)).getPlanByProgramId(programId);
    }

    @Test
    void findPlanByProgramId_shouldReturnEmptyWhenClientReturnsNull() {
        UUID programId = UUID.randomUUID();
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(null);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.findPlanByProgramId(programId);

        assertThat(result).isEmpty();
    }

    @Test
    void findPlanByProgramId_shouldReturnEmptyWhenPlanNotFound() {
        UUID programId = UUID.randomUUID();
        FeignException.NotFound exception = mock(FeignException.NotFound.class);
        when(nutritionClient.getPlanByProgramId(programId)).thenThrow(exception);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.findPlanByProgramId(programId);

        assertThat(result).isEmpty();
    }

    @Test
    void findPlanByProgramId_shouldReturnEmptyWhenFeignServiceUnavailable() {
        UUID programId = UUID.randomUUID();
        FeignException exception = mock(FeignException.class);
        when(nutritionClient.getPlanByProgramId(programId)).thenThrow(exception);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.findPlanByProgramId(programId);

        assertThat(result).isEmpty();
    }

    @Test
    void findPlanByProgramId_shouldReturnEmptyWhenUnexpectedRuntimeExceptionOccurs() {
        UUID programId = UUID.randomUUID();
        when(nutritionClient.getPlanByProgramId(programId)).thenThrow(new RuntimeException("Unexpected failure"));

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.findPlanByProgramId(programId);

        assertThat(result).isEmpty();
    }

    @Test
    void createPlan_shouldReturnCreatedPlanWhenClientSucceeds() {
        UUID programId = UUID.randomUUID();
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        NutritionPlanResponseDto responseDto = nutritionPlanResponseDto(UUID.randomUUID(), programId);
        when(nutritionClient.createPlan(requestDto)).thenReturn(responseDto);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(requestDto);

        assertThat(result).containsSame(responseDto);
        verify(nutritionClient, times(1)).createPlan(requestDto);
    }

    @Test
    void createPlan_shouldReturnEmptyWhenClientReturnsNull() {
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(UUID.randomUUID());
        when(nutritionClient.createPlan(requestDto)).thenReturn(null);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(requestDto);

        assertThat(result).isEmpty();
    }

    @Test
    void createPlan_shouldReturnEmptyWhenFeignCallFails() {
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(UUID.randomUUID());
        FeignException exception = mock(FeignException.class);
        when(nutritionClient.createPlan(requestDto)).thenThrow(exception);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(requestDto);

        assertThat(result).isEmpty();
    }

    @Test
    void addMealToPlan_shouldReturnMealWhenClientSucceeds() {
        UUID planId = UUID.randomUUID();
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        MealEntryResponseDto responseDto = mealEntryResponseDto(UUID.randomUUID());
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenReturn(responseDto);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(planId, requestDto);

        assertThat(result).containsSame(responseDto);
        verify(nutritionClient, times(1)).addMealToPlan(planId, requestDto);
    }

    @Test
    void addMealToPlan_shouldReturnEmptyWhenClientReturnsNull() {
        UUID planId = UUID.randomUUID();
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenReturn(null);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(planId, requestDto);

        assertThat(result).isEmpty();
    }

    @Test
    void addMealToPlan_shouldReturnEmptyWhenFeignCallFails() {
        UUID planId = UUID.randomUUID();
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        FeignException exception = mock(FeignException.class);
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenThrow(exception);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(planId, requestDto);

        assertThat(result).isEmpty();
    }

    private static NutritionPlanRequestDto nutritionPlanRequestDto(UUID programId) {
        NutritionPlanRequestDto dto = new NutritionPlanRequestDto();
        dto.setProgramId(programId);
        dto.setName("Balanced Plan");
        dto.setDescription("Balanced nutrition plan.");
        dto.setDailyCalories(2200);
        return dto;
    }

    private static NutritionPlanResponseDto nutritionPlanResponseDto(UUID id, UUID programId) {
        NutritionPlanResponseDto dto = new NutritionPlanResponseDto();
        dto.setId(id);
        dto.setProgramId(programId);
        dto.setName("Balanced Plan");
        dto.setDescription("Balanced nutrition plan.");
        dto.setDailyCalories(2200);
        return dto;
    }

    private static MealEntryRequestDto mealEntryRequestDto() {
        MealEntryRequestDto dto = new MealEntryRequestDto();
        dto.setMealName("Breakfast");
        dto.setCalories(500);
        dto.setProtein(30);
        dto.setCarbs(55);
        dto.setFats(15);
        dto.setDayOfWeek("MONDAY");
        return dto;
    }

    private static MealEntryResponseDto mealEntryResponseDto(UUID id) {
        MealEntryResponseDto dto = new MealEntryResponseDto();
        dto.setId(id);
        dto.setMealName("Breakfast");
        dto.setCalories(500);
        dto.setProtein(30);
        dto.setCarbs(55);
        dto.setFats(15);
        dto.setDayOfWeek("MONDAY");
        return dto;
    }
}
