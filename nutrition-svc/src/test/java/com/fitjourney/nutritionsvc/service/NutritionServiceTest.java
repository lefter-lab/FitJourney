package com.fitjourney.nutritionsvc.service;

import com.fitjourney.nutritionsvc.dto.MealEntryDto;
import com.fitjourney.nutritionsvc.dto.MealEntryResponseDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanResponseDto;
import com.fitjourney.nutritionsvc.entity.DayOfWeek;
import com.fitjourney.nutritionsvc.entity.MealEntry;
import com.fitjourney.nutritionsvc.entity.NutritionPlan;
import com.fitjourney.nutritionsvc.exception.DuplicatePlanException;
import com.fitjourney.nutritionsvc.exception.NutritionPlanNotFoundException;
import com.fitjourney.nutritionsvc.repository.MealEntryRepository;
import com.fitjourney.nutritionsvc.repository.NutritionPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private NutritionPlanRepository nutritionPlanRepository;

    @Mock
    private MealEntryRepository mealEntryRepository;

    @InjectMocks
    private NutritionService nutritionService;

    @Test
    void createPlan_shouldMapDtoSavePlanAndReturnResponse() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        NutritionPlanDto dto = nutritionPlanDto(programId);

        when(nutritionPlanRepository.existsByProgramId(programId)).thenReturn(false);
        when(nutritionPlanRepository.save(any(NutritionPlan.class)))
                .thenAnswer(invocation -> {
                    NutritionPlan savedPlan = invocation.getArgument(0);
                    savedPlan.setId(planId);
                    return savedPlan;
                });
        when(mealEntryRepository.findAllByNutritionPlanId(planId)).thenReturn(List.of());

        NutritionPlanResponseDto response = nutritionService.createPlan(dto);

        ArgumentCaptor<NutritionPlan> captor = ArgumentCaptor.forClass(NutritionPlan.class);
        verify(nutritionPlanRepository, times(1)).existsByProgramId(programId);
        verify(nutritionPlanRepository, times(1)).save(captor.capture());
        verify(mealEntryRepository, times(1)).findAllByNutritionPlanId(planId);

        NutritionPlan savedPlan = captor.getValue();
        assertThat(savedPlan.getProgramId()).isEqualTo(programId);
        assertThat(savedPlan.getName()).isEqualTo("Balanced Plan");
        assertThat(savedPlan.getDescription()).isEqualTo("Balanced nutrition plan");
        assertThat(savedPlan.getDailyCalories()).isEqualTo(2200);
        assertThat(savedPlan.getCreatedAt()).isNotNull();

        assertThat(response.getId()).isEqualTo(planId);
        assertThat(response.getProgramId()).isEqualTo(programId);
        assertThat(response.getName()).isEqualTo("Balanced Plan");
        assertThat(response.getDescription()).isEqualTo("Balanced nutrition plan");
        assertThat(response.getDailyCalories()).isEqualTo(2200);
        assertThat(response.getMeals()).isEmpty();
    }

    @Test
    void createPlan_shouldThrowDuplicatePlanExceptionWhenProgramAlreadyHasPlan() {
        UUID programId = UUID.randomUUID();
        NutritionPlanDto dto = nutritionPlanDto(programId);
        when(nutritionPlanRepository.existsByProgramId(programId)).thenReturn(true);

        assertThatThrownBy(() -> nutritionService.createPlan(dto))
                .isInstanceOf(DuplicatePlanException.class)
                .hasMessage("A nutrition plan already exists for this workout program.");

        verify(nutritionPlanRepository, times(1)).existsByProgramId(programId);
        verify(nutritionPlanRepository, never()).save(any(NutritionPlan.class));
        verify(mealEntryRepository, never()).findAllByNutritionPlanId(any(UUID.class));
    }

    @Test
    void getPlanByProgramId_shouldReturnMappedPlanWithMealsWhenFound() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        NutritionPlan plan = nutritionPlan(planId, programId);
        MealEntry breakfast = mealEntry(UUID.randomUUID(), plan, "Breakfast", 500, 30, 45, 12, DayOfWeek.MONDAY);
        MealEntry dinner = mealEntry(UUID.randomUUID(), plan, "Dinner", 700, 40, 60, 20, DayOfWeek.FRIDAY);

        when(nutritionPlanRepository.findByProgramId(programId)).thenReturn(Optional.of(plan));
        when(mealEntryRepository.findAllByNutritionPlanId(planId)).thenReturn(List.of(breakfast, dinner));

        NutritionPlanResponseDto response = nutritionService.getPlanByProgramId(programId);

        verify(nutritionPlanRepository, times(1)).findByProgramId(programId);
        verify(mealEntryRepository, times(1)).findAllByNutritionPlanId(planId);

        assertThat(response.getId()).isEqualTo(planId);
        assertThat(response.getProgramId()).isEqualTo(programId);
        assertThat(response.getName()).isEqualTo("Balanced Plan");
        assertThat(response.getDescription()).isEqualTo("Balanced nutrition plan");
        assertThat(response.getDailyCalories()).isEqualTo(2200);
        assertThat(response.getMeals()).hasSize(2);
        assertThat(response.getMeals())
                .extracting(MealEntryResponseDto::getMealName)
                .containsExactly("Breakfast", "Dinner");
        assertThat(response.getMeals().get(0).getCalories()).isEqualTo(500);
        assertThat(response.getMeals().get(0).getProtein()).isEqualTo(30);
        assertThat(response.getMeals().get(0).getCarbs()).isEqualTo(45);
        assertThat(response.getMeals().get(0).getFats()).isEqualTo(12);
        assertThat(response.getMeals().get(0).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void getPlanByProgramId_shouldThrowNutritionPlanNotFoundExceptionWhenMissing() {
        UUID programId = UUID.randomUUID();
        when(nutritionPlanRepository.findByProgramId(programId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nutritionService.getPlanByProgramId(programId))
                .isInstanceOf(NutritionPlanNotFoundException.class)
                .hasMessage("Nutrition plan not found for program ID: " + programId);

        verify(nutritionPlanRepository, times(1)).findByProgramId(programId);
        verify(mealEntryRepository, never()).findAllByNutritionPlanId(any(UUID.class));
    }

    @Test
    void addMealEntry_shouldMapDtoSaveMealAndReturnResponse() {
        UUID planId = UUID.randomUUID();
        UUID mealId = UUID.randomUUID();
        NutritionPlan plan = nutritionPlan(planId, UUID.randomUUID());
        MealEntryDto dto = mealEntryDto();

        when(nutritionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(mealEntryRepository.save(any(MealEntry.class)))
                .thenAnswer(invocation -> {
                    MealEntry savedEntry = invocation.getArgument(0);
                    savedEntry.setId(mealId);
                    return savedEntry;
                });

        MealEntryResponseDto response = nutritionService.addMealEntry(planId, dto);

        ArgumentCaptor<MealEntry> captor = ArgumentCaptor.forClass(MealEntry.class);
        verify(nutritionPlanRepository, times(1)).findById(planId);
        verify(mealEntryRepository, times(1)).save(captor.capture());

        MealEntry savedEntry = captor.getValue();
        assertThat(savedEntry.getNutritionPlan()).isSameAs(plan);
        assertThat(savedEntry.getMealName()).isEqualTo("Breakfast");
        assertThat(savedEntry.getCalories()).isEqualTo(500);
        assertThat(savedEntry.getProtein()).isEqualTo(30);
        assertThat(savedEntry.getCarbs()).isEqualTo(45);
        assertThat(savedEntry.getFats()).isEqualTo(12);
        assertThat(savedEntry.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        assertThat(response.getId()).isEqualTo(mealId);
        assertThat(response.getMealName()).isEqualTo("Breakfast");
        assertThat(response.getCalories()).isEqualTo(500);
        assertThat(response.getProtein()).isEqualTo(30);
        assertThat(response.getCarbs()).isEqualTo(45);
        assertThat(response.getFats()).isEqualTo(12);
        assertThat(response.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void addMealEntry_shouldThrowNutritionPlanNotFoundExceptionWhenPlanIsMissing() {
        UUID planId = UUID.randomUUID();
        MealEntryDto dto = mealEntryDto();
        when(nutritionPlanRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nutritionService.addMealEntry(planId, dto))
                .isInstanceOf(NutritionPlanNotFoundException.class)
                .hasMessage("Nutrition plan not found with ID: " + planId);

        verify(nutritionPlanRepository, times(1)).findById(planId);
        verify(mealEntryRepository, never()).save(any(MealEntry.class));
    }

    private static NutritionPlanDto nutritionPlanDto(UUID programId) {
        NutritionPlanDto dto = new NutritionPlanDto();
        dto.setProgramId(programId);
        dto.setName("Balanced Plan");
        dto.setDescription("Balanced nutrition plan");
        dto.setDailyCalories(2200);
        return dto;
    }

    private static MealEntryDto mealEntryDto() {
        MealEntryDto dto = new MealEntryDto();
        dto.setMealName("Breakfast");
        dto.setCalories(500);
        dto.setProtein(30);
        dto.setCarbs(45);
        dto.setFats(12);
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        return dto;
    }

    private static NutritionPlan nutritionPlan(UUID id, UUID programId) {
        NutritionPlan plan = new NutritionPlan();
        plan.setId(id);
        plan.setProgramId(programId);
        plan.setName("Balanced Plan");
        plan.setDescription("Balanced nutrition plan");
        plan.setDailyCalories(2200);
        plan.setCreatedAt(LocalDateTime.now());
        return plan;
    }

    private static MealEntry mealEntry(UUID id,
                                       NutritionPlan plan,
                                       String mealName,
                                       int calories,
                                       int protein,
                                       int carbs,
                                       int fats,
                                       DayOfWeek dayOfWeek) {
        MealEntry entry = new MealEntry();
        entry.setId(id);
        entry.setNutritionPlan(plan);
        entry.setMealName(mealName);
        entry.setCalories(calories);
        entry.setProtein(protein);
        entry.setCarbs(carbs);
        entry.setFats(fats);
        entry.setDayOfWeek(dayOfWeek);
        return entry;
    }
}
