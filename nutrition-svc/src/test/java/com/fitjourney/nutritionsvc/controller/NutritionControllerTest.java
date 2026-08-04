package com.fitjourney.nutritionsvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitjourney.nutritionsvc.dto.MealEntryDto;
import com.fitjourney.nutritionsvc.dto.MealEntryResponseDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanDto;
import com.fitjourney.nutritionsvc.dto.NutritionPlanResponseDto;
import com.fitjourney.nutritionsvc.entity.DayOfWeek;
import com.fitjourney.nutritionsvc.exception.DuplicatePlanException;
import com.fitjourney.nutritionsvc.exception.GlobalExceptionHandler;
import com.fitjourney.nutritionsvc.exception.NutritionPlanNotFoundException;
import com.fitjourney.nutritionsvc.service.NutritionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutritionController.class)
@Import(GlobalExceptionHandler.class)
class NutritionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NutritionService nutritionService;

    @Test
    void getPlanByProgramId_shouldReturnPlanWhenFound() throws Exception {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        MealEntryResponseDto meal = mealEntryResponseDto();
        NutritionPlanResponseDto responseDto = nutritionPlanResponseDto(planId, programId, List.of(meal));
        when(nutritionService.getPlanByProgramId(programId)).thenReturn(responseDto);

        mockMvc.perform(get("/nutrition/programs/{programId}", programId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.programId").value(programId.toString()))
                .andExpect(jsonPath("$.name").value("Balanced Plan"))
                .andExpect(jsonPath("$.description").value("Balanced nutrition plan"))
                .andExpect(jsonPath("$.dailyCalories").value(2200))
                .andExpect(jsonPath("$.meals[0].id").value(meal.getId().toString()))
                .andExpect(jsonPath("$.meals[0].mealName").value("Breakfast"))
                .andExpect(jsonPath("$.meals[0].calories").value(500))
                .andExpect(jsonPath("$.meals[0].protein").value(30))
                .andExpect(jsonPath("$.meals[0].carbs").value(45))
                .andExpect(jsonPath("$.meals[0].fats").value(12))
                .andExpect(jsonPath("$.meals[0].dayOfWeek").value("MONDAY"));

        verify(nutritionService, times(1)).getPlanByProgramId(programId);
    }

    @Test
    void getPlanByProgramId_shouldReturnNotFoundWhenPlanIsMissing() throws Exception {
        UUID programId = UUID.randomUUID();
        when(nutritionService.getPlanByProgramId(programId))
                .thenThrow(new NutritionPlanNotFoundException("Nutrition plan not found for program ID: " + programId));

        mockMvc.perform(get("/nutrition/programs/{programId}", programId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Nutrition plan not found for program ID: " + programId))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, times(1)).getPlanByProgramId(programId);
    }

    @Test
    void getPlanByProgramId_shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        UUID programId = UUID.randomUUID();
        when(nutritionService.getPlanByProgramId(programId)).thenThrow(new RuntimeException("Database unavailable"));

        mockMvc.perform(get("/nutrition/programs/{programId}", programId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, times(1)).getPlanByProgramId(programId);
    }

    @Test
    void createPlan_shouldReturnCreatedPlanWhenRequestIsValid() throws Exception {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        NutritionPlanDto requestDto = nutritionPlanDto(programId);
        NutritionPlanResponseDto responseDto = nutritionPlanResponseDto(planId, programId, List.of());
        when(nutritionService.createPlan(any(NutritionPlanDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/nutrition/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.programId").value(programId.toString()))
                .andExpect(jsonPath("$.name").value("Balanced Plan"))
                .andExpect(jsonPath("$.description").value("Balanced nutrition plan"))
                .andExpect(jsonPath("$.dailyCalories").value(2200))
                .andExpect(jsonPath("$.meals").isArray());

        ArgumentCaptor<NutritionPlanDto> captor = ArgumentCaptor.forClass(NutritionPlanDto.class);
        verify(nutritionService, times(1)).createPlan(captor.capture());
        assertThat(captor.getValue().getProgramId()).isEqualTo(programId);
        assertThat(captor.getValue().getName()).isEqualTo("Balanced Plan");
        assertThat(captor.getValue().getDescription()).isEqualTo("Balanced nutrition plan");
        assertThat(captor.getValue().getDailyCalories()).isEqualTo(2200);
    }

    @Test
    void createPlan_shouldReturnConflictWhenPlanAlreadyExists() throws Exception {
        UUID programId = UUID.randomUUID();
        NutritionPlanDto requestDto = nutritionPlanDto(programId);
        when(nutritionService.createPlan(any(NutritionPlanDto.class)))
                .thenThrow(new DuplicatePlanException("A nutrition plan already exists for this workout program."));

        mockMvc.perform(post("/nutrition/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A nutrition plan already exists for this workout program."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, times(1)).createPlan(any(NutritionPlanDto.class));
    }

    @Test
    void createPlan_shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        NutritionPlanDto requestDto = new NutritionPlanDto();
        requestDto.setName("");
        requestDto.setDescription("Balanced nutrition plan");
        requestDto.setDailyCalories(0);

        mockMvc.perform(post("/nutrition/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.programId").value("Program ID is required"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.dailyCalories").value("Daily calories must be positive"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, never()).createPlan(any(NutritionPlanDto.class));
    }

    @Test
    void addMealEntry_shouldReturnCreatedMealWhenRequestIsValid() throws Exception {
        UUID planId = UUID.randomUUID();
        MealEntryDto requestDto = mealEntryDto();
        MealEntryResponseDto responseDto = mealEntryResponseDto();
        when(nutritionService.addMealEntry(eq(planId), any(MealEntryDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/nutrition/plans/{planId}/meals", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()))
                .andExpect(jsonPath("$.mealName").value("Breakfast"))
                .andExpect(jsonPath("$.calories").value(500))
                .andExpect(jsonPath("$.protein").value(30))
                .andExpect(jsonPath("$.carbs").value(45))
                .andExpect(jsonPath("$.fats").value(12))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));

        ArgumentCaptor<MealEntryDto> captor = ArgumentCaptor.forClass(MealEntryDto.class);
        verify(nutritionService, times(1)).addMealEntry(eq(planId), captor.capture());
        assertThat(captor.getValue().getMealName()).isEqualTo("Breakfast");
        assertThat(captor.getValue().getCalories()).isEqualTo(500);
        assertThat(captor.getValue().getProtein()).isEqualTo(30);
        assertThat(captor.getValue().getCarbs()).isEqualTo(45);
        assertThat(captor.getValue().getFats()).isEqualTo(12);
        assertThat(captor.getValue().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void addMealEntry_shouldReturnNotFoundWhenPlanIsMissing() throws Exception {
        UUID planId = UUID.randomUUID();
        MealEntryDto requestDto = mealEntryDto();
        when(nutritionService.addMealEntry(eq(planId), any(MealEntryDto.class)))
                .thenThrow(new NutritionPlanNotFoundException("Nutrition plan not found with ID: " + planId));

        mockMvc.perform(post("/nutrition/plans/{planId}/meals", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Nutrition plan not found with ID: " + planId))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, times(1)).addMealEntry(eq(planId), any(MealEntryDto.class));
    }

    @Test
    void addMealEntry_shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        MealEntryDto requestDto = new MealEntryDto();
        requestDto.setMealName("");
        requestDto.setCalories(0);
        requestDto.setProtein(-1);
        requestDto.setCarbs(-1);
        requestDto.setFats(-1);

        mockMvc.perform(post("/nutrition/plans/{planId}/meals", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.mealName").exists())
                .andExpect(jsonPath("$.errors.calories").value("Calories must be positive"))
                .andExpect(jsonPath("$.errors.protein").value("Protein cannot be negative"))
                .andExpect(jsonPath("$.errors.carbs").value("Carbs cannot be negative"))
                .andExpect(jsonPath("$.errors.fats").value("Fats cannot be negative"))
                .andExpect(jsonPath("$.errors.dayOfWeek").value("Day of week is required"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, never()).addMealEntry(any(UUID.class), any(MealEntryDto.class));
    }

    @Test
    void addMealEntry_shouldReturnBadRequestWhenDayOfWeekCannotBeParsed() throws Exception {
        UUID planId = UUID.randomUUID();

        String requestBody = """
                {
                  "mealName": "Breakfast",
                  "calories": 500,
                  "protein": 30,
                  "carbs": 45,
                  "fats": 12,
                  "dayOfWeek": "INVALID_DAY"
                }
                """;

        mockMvc.perform(post("/nutrition/plans/{planId}/meals", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable JSON request."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, never())
                .addMealEntry(any(UUID.class), any(MealEntryDto.class));
    }

    @Test
    void addMealEntry_shouldReturnBadRequestWhenMealNameIsTooShort() throws Exception {
        MealEntryDto requestDto = mealEntryDto();
        requestDto.setMealName("A");

        mockMvc.perform(post("/nutrition/plans/{planId}/meals", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.mealName")
                        .value("Meal name must be between 2 and 100 characters"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, never()).addMealEntry(any(UUID.class), any(MealEntryDto.class));
    }

    @Test
    void addMealEntry_shouldReturnBadRequestWhenMealNameIsTooLong() throws Exception {
        MealEntryDto requestDto = mealEntryDto();
        requestDto.setMealName("A".repeat(101));

        mockMvc.perform(post("/nutrition/plans/{planId}/meals", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.mealName")
                        .value("Meal name must be between 2 and 100 characters"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nutritionService, never()).addMealEntry(any(UUID.class), any(MealEntryDto.class));
    }

    private static NutritionPlanDto nutritionPlanDto(UUID programId) {
        NutritionPlanDto dto = new NutritionPlanDto();
        dto.setProgramId(programId);
        dto.setName("Balanced Plan");
        dto.setDescription("Balanced nutrition plan");
        dto.setDailyCalories(2200);
        return dto;
    }

    private static NutritionPlanResponseDto nutritionPlanResponseDto(UUID planId,
                                                                     UUID programId,
                                                                     List<MealEntryResponseDto> meals) {
        NutritionPlanResponseDto dto = new NutritionPlanResponseDto();
        dto.setId(planId);
        dto.setProgramId(programId);
        dto.setName("Balanced Plan");
        dto.setDescription("Balanced nutrition plan");
        dto.setDailyCalories(2200);
        dto.setMeals(meals);
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

    private static MealEntryResponseDto mealEntryResponseDto() {
        MealEntryResponseDto dto = new MealEntryResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setMealName("Breakfast");
        dto.setCalories(500);
        dto.setProtein(30);
        dto.setCarbs(45);
        dto.setFats(12);
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        return dto;
    }
}
