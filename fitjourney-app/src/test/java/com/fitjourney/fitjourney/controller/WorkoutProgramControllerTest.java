package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.client.dto.MealEntryRequestDto;
import com.fitjourney.fitjourney.client.dto.MealEntryResponseDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanRequestDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanResponseDto;
import com.fitjourney.fitjourney.config.SecurityConfig;
import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.enums.UserRole;
import com.fitjourney.fitjourney.exception.UnauthorizedProgramAccessException;
import com.fitjourney.fitjourney.security.CustomAuthenticationSuccessHandler;
import com.fitjourney.fitjourney.service.NutritionIntegrationService;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WorkoutProgramController.class)
@Import(SecurityConfig.class)
class WorkoutProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutProgramService workoutProgramService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NutritionIntegrationService nutritionIntegrationService;

    @MockitoBean
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Test
    @WithMockUser
    void showProgramDetails_shouldReturnDetailsViewWithNutritionModels() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId, trainer(UUID.randomUUID(), "trainer"));
        NutritionPlanResponseDto nutritionPlan = nutritionPlanResponse(programId);
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(nutritionIntegrationService.findPlanByProgramId(programId)).thenReturn(Optional.of(nutritionPlan));

        mockMvc.perform(get("/programs/{id}", programId))
                .andExpect(status().isOk())
                .andExpect(view().name("programs/program-details"))
                .andExpect(model().attribute("program", sameInstance(program)))
                .andExpect(model().attribute("nutritionPlan", sameInstance(nutritionPlan)))
                .andExpect(model().attribute("nutritionPlanForm", instanceOf(NutritionPlanRequestDto.class)))
                .andExpect(model().attribute("mealEntryForm", instanceOf(MealEntryRequestDto.class)))
                .andExpect(model().attribute("daysOfWeek", hasSize(7)));

        verify(workoutProgramService, times(1)).findById(programId);
        verify(nutritionIntegrationService, times(1)).findPlanByProgramId(programId);
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void showCreateForm_shouldReturnCreateViewForTrainer() throws Exception {
        mockMvc.perform(get("/programs/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("programs/program-create"))
                .andExpect(model().attribute("programDto", instanceOf(WorkoutProgramDto.class)))
                .andExpect(model().attributeExists("levels"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void showCreateForm_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(get("/programs/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    void showCreateForm_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/programs/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void createProgram_shouldRedirectToAllProgramsWhenTrainerSubmitsValidForm() throws Exception {
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);

        mockMvc.perform(post("/programs/create")
                        .with(csrf())
                        .param("name", "Strength Plan")
                        .param("description", "Complete strength program")
                        .param("difficultyLevel", "INTERMEDIATE")
                        .param("durationWeeks", "8")
                        .param("price", "49.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"));

        ArgumentCaptor<WorkoutProgramDto> captor = ArgumentCaptor.forClass(WorkoutProgramDto.class);
        verify(userService, times(1)).findByUsername("trainer");
        verify(workoutProgramService, times(1)).createProgram(captor.capture(), eq(trainer));

        WorkoutProgramDto dto = captor.getValue();
        assertThat(dto.getName()).isEqualTo("Strength Plan");
        assertThat(dto.getDescription()).isEqualTo("Complete strength program");
        assertThat(dto.getDifficultyLevel()).isEqualTo(DifficultyLevel.INTERMEDIATE);
        assertThat(dto.getDurationWeeks()).isEqualTo(8);
        assertThat(dto.getPrice()).isEqualTo(49.99);
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void createProgram_shouldReturnCreateViewWhenValidationFails() throws Exception {
        mockMvc.perform(post("/programs/create")
                        .with(csrf())
                        .param("name", "")
                        .param("description", "short")
                        .param("durationWeeks", "0")
                        .param("price", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("programs/program-create"))
                .andExpect(model().attributeExists("levels"))
                .andExpect(model().attributeHasFieldErrors(
                        "programDto",
                        "name",
                        "description",
                        "difficultyLevel",
                        "durationWeeks",
                        "price"));

        verify(userService, never()).findByUsername(any());
        verify(workoutProgramService, never()).createProgram(any(WorkoutProgramDto.class), any(User.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProgram_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(post("/programs/create")
                        .with(csrf())
                        .param("name", "Strength Plan")
                        .param("description", "Complete strength program")
                        .param("difficultyLevel", "BEGINNER")
                        .param("durationWeeks", "6")
                        .param("price", "25"))
                .andExpect(status().isForbidden());

        verify(workoutProgramService, never()).createProgram(any(WorkoutProgramDto.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "johnny")
    void getAllPrograms_shouldReturnProgramsViewForAuthenticatedUser() throws Exception {
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        List<WorkoutProgram> programs = List.of(
                workoutProgram(UUID.randomUUID(), trainer(UUID.randomUUID(), "trainer-one")),
                workoutProgram(UUID.randomUUID(), trainer(UUID.randomUUID(), "trainer-two")));
        when(workoutProgramService.getAllPrograms()).thenReturn(programs);
        when(userService.findByUsername("johnny")).thenReturn(user);

        mockMvc.perform(get("/programs/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("programs/programs-all"))
                .andExpect(model().attribute("programs", sameInstance(programs)))
                .andExpect(model().attribute("currentUserId", user.getId()));

        verify(workoutProgramService, times(1)).getAllPrograms();
        verify(userService, times(1)).findByUsername("johnny");
    }

    @Test
    void getAllPrograms_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/programs/all"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void showEditForm_shouldReturnEditViewWhenTrainerOwnsProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        WorkoutProgram program = workoutProgram(programId, trainer);
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(userService.findByUsername("trainer")).thenReturn(trainer);

        mockMvc.perform(get("/programs/{id}/edit", programId))
                .andExpect(status().isOk())
                .andExpect(view().name("programs/program-edit"))
                .andExpect(model().attribute("programDto", instanceOf(WorkoutProgramDto.class)))
                .andExpect(model().attributeExists("levels"))
                .andExpect(model().attribute("programId", programId));

        ArgumentCaptor<WorkoutProgram> programCaptor = ArgumentCaptor.forClass(WorkoutProgram.class);
        ArgumentCaptor<User> trainerCaptor = ArgumentCaptor.forClass(User.class);
        verify(workoutProgramService, times(1)).verifyTrainerOwnership(programCaptor.capture(), trainerCaptor.capture());
        assertThat(programCaptor.getValue()).isSameAs(program);
        assertThat(trainerCaptor.getValue()).isSameAs(trainer);
    }

    @Test
    @WithMockUser(roles = "USER")
    void showEditForm_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(get("/programs/{id}/edit", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void editProgram_shouldRedirectToAllProgramsWhenUpdateSucceeds() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);

        mockMvc.perform(post("/programs/{id}/edit", programId)
                        .with(csrf())
                        .param("name", "Updated Plan")
                        .param("description", "Updated program description")
                        .param("difficultyLevel", "ADVANCED")
                        .param("durationWeeks", "10")
                        .param("price", "99.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"));

        ArgumentCaptor<WorkoutProgramDto> captor = ArgumentCaptor.forClass(WorkoutProgramDto.class);
        verify(userService, times(1)).findByUsername("trainer");
        verify(workoutProgramService, times(1)).updateProgram(eq(programId), captor.capture(), eq(trainer));
        assertThat(captor.getValue().getName()).isEqualTo("Updated Plan");
        assertThat(captor.getValue().getDescription()).isEqualTo("Updated program description");
        assertThat(captor.getValue().getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
        assertThat(captor.getValue().getDurationWeeks()).isEqualTo(10);
        assertThat(captor.getValue().getPrice()).isEqualTo(99.5);
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void editProgram_shouldReturnEditViewWhenValidationFails() throws Exception {
        UUID programId = UUID.randomUUID();

        mockMvc.perform(post("/programs/{id}/edit", programId)
                        .with(csrf())
                        .param("name", "Bad")
                        .param("description", "short")
                        .param("durationWeeks", "0")
                        .param("price", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("programs/program-edit"))
                .andExpect(model().attributeExists("levels"))
                .andExpect(model().attribute("programId", programId))
                .andExpect(model().attributeHasFieldErrors(
                        "programDto",
                        "name",
                        "description",
                        "difficultyLevel",
                        "durationWeeks",
                        "price"));

        verify(workoutProgramService, never()).updateProgram(any(), any(WorkoutProgramDto.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void editProgram_shouldRedirectWithErrorMessageWhenTrainerDoesNotOwnProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);
        doThrow(new UnauthorizedProgramAccessException("You can only manage your own workout programs"))
                .when(workoutProgramService)
                .updateProgram(eq(programId), any(WorkoutProgramDto.class), eq(trainer));

        mockMvc.perform(post("/programs/{id}/edit", programId)
                        .with(csrf())
                        .param("name", "Updated Plan")
                        .param("description", "Updated program description")
                        .param("difficultyLevel", "ADVANCED")
                        .param("durationWeeks", "10")
                        .param("price", "99.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"))
                .andExpect(flash().attribute("errorMessage", "You can only manage your own workout programs"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void editProgram_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(post("/programs/{id}/edit", UUID.randomUUID())
                        .with(csrf())
                        .param("name", "Updated Plan")
                        .param("description", "Updated program description")
                        .param("difficultyLevel", "BEGINNER")
                        .param("durationWeeks", "4")
                        .param("price", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void deactivateProgram_shouldRedirectToAllProgramsWhenTrainerOwnsProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);

        mockMvc.perform(post("/programs/{id}/deactivate", programId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"));

        verify(workoutProgramService, times(1)).deactivateProgram(programId, trainer);
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void deactivateProgram_shouldRedirectWithErrorMessageWhenTrainerDoesNotOwnProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);
        doThrow(new UnauthorizedProgramAccessException("You can only manage your own workout programs"))
                .when(workoutProgramService)
                .deactivateProgram(programId, trainer);

        mockMvc.perform(post("/programs/{id}/deactivate", programId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"))
                .andExpect(flash().attribute("errorMessage", "You can only manage your own workout programs"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deactivateProgram_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(post("/programs/{id}/deactivate", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void deleteProgram_shouldRedirectToAllProgramsWhenTrainerOwnsProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);

        mockMvc.perform(post("/programs/{id}/delete", programId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"));

        verify(workoutProgramService, times(1)).deleteProgram(programId, trainer);
    }

    @Test
    @WithMockUser(username = "trainer", roles = "TRAINER")
    void deleteProgram_shouldRedirectWithErrorMessageWhenTrainerDoesNotOwnProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        User trainer = trainer(UUID.randomUUID(), "trainer");
        when(userService.findByUsername("trainer")).thenReturn(trainer);
        doThrow(new UnauthorizedProgramAccessException("You can only manage your own workout programs"))
                .when(workoutProgramService)
                .deleteProgram(programId, trainer);

        mockMvc.perform(post("/programs/{id}/delete", programId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"))
                .andExpect(flash().attribute("errorMessage", "You can only manage your own workout programs"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteProgram_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(post("/programs/{id}/delete", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void createNutritionPlan_shouldRedirectWithSuccessMessageWhenPlanIsCreated() throws Exception {
        UUID programId = UUID.randomUUID();
        NutritionPlanResponseDto responseDto = nutritionPlanResponse(programId);
        when(nutritionIntegrationService.createPlan(any(NutritionPlanRequestDto.class)))
                .thenReturn(Optional.of(responseDto));

        mockMvc.perform(post("/programs/{id}/nutrition-plan", programId)
                        .with(csrf())
                        .param("programId", programId.toString())
                        .param("name", "Balanced Plan")
                        .param("description", "Balanced nutrition plan")
                        .param("dailyCalories", "2200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/" + programId))
                .andExpect(flash().attribute("successMessage", "Nutrition plan created successfully."));

        ArgumentCaptor<NutritionPlanRequestDto> captor = ArgumentCaptor.forClass(NutritionPlanRequestDto.class);
        verify(nutritionIntegrationService, times(1)).createPlan(captor.capture());
        assertThat(captor.getValue().getProgramId()).isEqualTo(programId);
        assertThat(captor.getValue().getName()).isEqualTo("Balanced Plan");
        assertThat(captor.getValue().getDailyCalories()).isEqualTo(2200);
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void createNutritionPlan_shouldRedirectWithErrorMessageWhenIntegrationReturnsEmpty() throws Exception {
        UUID programId = UUID.randomUUID();
        when(nutritionIntegrationService.createPlan(any(NutritionPlanRequestDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/programs/{id}/nutrition-plan", programId)
                        .with(csrf())
                        .param("programId", programId.toString())
                        .param("name", "Balanced Plan")
                        .param("description", "Balanced nutrition plan")
                        .param("dailyCalories", "2200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/" + programId))
                .andExpect(flash().attribute("errorMessage", "Nutrition plan could not be created right now."));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void createNutritionPlan_shouldRedirectWithNutritionPlanErrorWhenValidationFails() throws Exception {
        UUID programId = UUID.randomUUID();

        mockMvc.perform(post("/programs/{id}/nutrition-plan", programId)
                        .with(csrf())
                        .param("programId", programId.toString())
                        .param("name", "")
                        .param("description", "Balanced nutrition plan")
                        .param("dailyCalories", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/" + programId))
                .andExpect(flash().attribute("nutritionPlanError", "Please correct the nutrition plan form."));

        verify(nutritionIntegrationService, never()).createPlan(any(NutritionPlanRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createNutritionPlan_shouldBeForbiddenForUser() throws Exception {
        UUID programId = UUID.randomUUID();

        mockMvc.perform(post("/programs/{id}/nutrition-plan", programId)
                        .with(csrf())
                        .param("programId", programId.toString())
                        .param("name", "Balanced Plan")
                        .param("description", "Balanced nutrition plan")
                        .param("dailyCalories", "2200"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void addMealToNutritionPlan_shouldRedirectWithSuccessMessageWhenMealIsAdded() throws Exception {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        MealEntryResponseDto responseDto = mealEntryResponse();
        when(nutritionIntegrationService.addMealToPlan(eq(planId), any(MealEntryRequestDto.class)))
                .thenReturn(Optional.of(responseDto));

        mockMvc.perform(post("/programs/{programId}/nutrition-plan/{planId}/meals", programId, planId)
                        .with(csrf())
                        .param("mealName", "Breakfast")
                        .param("calories", "500")
                        .param("protein", "30")
                        .param("carbs", "45")
                        .param("fats", "12")
                        .param("dayOfWeek", "MONDAY"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/" + programId))
                .andExpect(flash().attribute("successMessage", "Meal added successfully."));

        ArgumentCaptor<MealEntryRequestDto> captor = ArgumentCaptor.forClass(MealEntryRequestDto.class);
        verify(nutritionIntegrationService, times(1)).addMealToPlan(eq(planId), captor.capture());
        assertThat(captor.getValue().getMealName()).isEqualTo("Breakfast");
        assertThat(captor.getValue().getCalories()).isEqualTo(500);
        assertThat(captor.getValue().getProtein()).isEqualTo(30);
        assertThat(captor.getValue().getCarbs()).isEqualTo(45);
        assertThat(captor.getValue().getFats()).isEqualTo(12);
        assertThat(captor.getValue().getDayOfWeek()).isEqualTo("MONDAY");
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void addMealToNutritionPlan_shouldRedirectWithErrorMessageWhenIntegrationReturnsEmpty() throws Exception {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        when(nutritionIntegrationService.addMealToPlan(eq(planId), any(MealEntryRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/programs/{programId}/nutrition-plan/{planId}/meals", programId, planId)
                        .with(csrf())
                        .param("mealName", "Breakfast")
                        .param("calories", "500")
                        .param("protein", "30")
                        .param("carbs", "45")
                        .param("fats", "12")
                        .param("dayOfWeek", "MONDAY"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/" + programId))
                .andExpect(flash().attribute("errorMessage", "Meal could not be added right now."));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void addMealToNutritionPlan_shouldRedirectWithErrorMessageWhenValidationFails() throws Exception {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        mockMvc.perform(post("/programs/{programId}/nutrition-plan/{planId}/meals", programId, planId)
                        .with(csrf())
                        .param("mealName", "")
                        .param("calories", "0")
                        .param("protein", "-1")
                        .param("carbs", "-1")
                        .param("fats", "-1")
                        .param("dayOfWeek", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/" + programId))
                .andExpect(flash().attribute("errorMessage", "Please correct the meal form."));

        verify(nutritionIntegrationService, never()).addMealToPlan(any(), any(MealEntryRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addMealToNutritionPlan_shouldBeForbiddenForUser() throws Exception {
        mockMvc.perform(post("/programs/{programId}/nutrition-plan/{planId}/meals", UUID.randomUUID(), UUID.randomUUID())
                        .with(csrf())
                        .param("mealName", "Breakfast")
                        .param("calories", "500")
                        .param("protein", "30")
                        .param("carbs", "45")
                        .param("fats", "12")
                        .param("dayOfWeek", "MONDAY"))
                .andExpect(status().isForbidden());
    }

    private static User trainer(UUID id, String username) {
        return user(id, username, UserRole.TRAINER);
    }

    private static User user(UUID id, String username, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole(role);
        return user;
    }

    private static WorkoutProgram workoutProgram(UUID id, User trainer) {
        WorkoutProgram program = new WorkoutProgram();
        program.setId(id);
        program.setTitle("Strength Plan");
        program.setDescription("Complete strength program");
        program.setDifficulty(DifficultyLevel.INTERMEDIATE);
        program.setDurationWeeks(8);
        program.setPrice(BigDecimal.valueOf(49.99));
        program.setActive(true);
        program.setTrainer(trainer);
        return program;
    }

    private static NutritionPlanResponseDto nutritionPlanResponse(UUID programId) {
        NutritionPlanResponseDto dto = new NutritionPlanResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setProgramId(programId);
        dto.setName("Balanced Plan");
        dto.setDescription("Balanced nutrition plan");
        dto.setDailyCalories(2200);
        dto.setMeals(List.of());
        return dto;
    }

    private static MealEntryResponseDto mealEntryResponse() {
        MealEntryResponseDto dto = new MealEntryResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setMealName("Breakfast");
        dto.setCalories(500);
        dto.setProtein(30);
        dto.setCarbs(45);
        dto.setFats(12);
        dto.setDayOfWeek("MONDAY");
        return dto;
    }
}
