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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionIntegrationServiceTest {

    @Mock
    private NutritionClient nutritionClient;

    @Mock
    private UserService userService;

    @Mock
    private WorkoutProgramService workoutProgramService;

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
    void createPlan_shouldAllowTrainerWhenTrainerOwnsProgram() {
        UUID programId = UUID.randomUUID();
        User trainer = user(UUID.randomUUID(), "trainer", UserRole.TRAINER);
        WorkoutProgram program = workoutProgram(programId, trainer);
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        NutritionPlanResponseDto responseDto = nutritionPlanResponseDto(UUID.randomUUID(), programId);
        allowUserForProgram(programId, "trainer", trainer, program);
        when(nutritionClient.createPlan(requestDto)).thenReturn(responseDto);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(programId, requestDto, "trainer");

        assertThat(result).containsSame(responseDto);
        verify(workoutProgramService, times(1)).verifyTrainerOwnership(program, trainer);
        verify(nutritionClient, times(1)).createPlan(requestDto);
    }

    @Test
    void createPlan_shouldAllowAdminForAnyProgram() {
        UUID programId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        NutritionPlanResponseDto responseDto = nutritionPlanResponseDto(UUID.randomUUID(), programId);
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.createPlan(requestDto)).thenReturn(responseDto);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(programId, requestDto, "admin");

        assertThat(result).containsSame(responseDto);
        verify(workoutProgramService, never()).verifyTrainerOwnership(any(WorkoutProgram.class), any(User.class));
        verify(nutritionClient, times(1)).createPlan(requestDto);
    }

    @Test
    void createPlan_shouldRejectTrainerWhenTrainerDoesNotOwnProgram() {
        UUID programId = UUID.randomUUID();
        User trainer = user(UUID.randomUUID(), "trainer", UserRole.TRAINER);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "owner", UserRole.TRAINER));
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        allowUserForProgram(programId, "trainer", trainer, program);
        doThrow(new UnauthorizedProgramAccessException("You can only manage your own workout programs"))
                .when(workoutProgramService)
                .verifyTrainerOwnership(program, trainer);

        assertThatThrownBy(() -> nutritionIntegrationService.createPlan(programId, requestDto, "trainer"))
                .isInstanceOf(UnauthorizedProgramAccessException.class)
                .hasMessage("You can only manage your own workout programs");

        verify(nutritionClient, never()).createPlan(any());
    }

    @Test
    void createPlan_shouldNotInvokeFeignWhenAccessIsDenied() {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "user", UserRole.USER);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        allowUserForProgram(programId, "user", user, program);

        assertThatThrownBy(() -> nutritionIntegrationService.createPlan(programId, requestDto, "user"))
                .isInstanceOf(UnauthorizedProgramAccessException.class);

        verify(nutritionClient, never()).getPlanByProgramId(any());
        verify(nutritionClient, never()).createPlan(any());
        verify(nutritionClient, never()).addMealToPlan(any(), any());
    }

    @Test
    void createPlan_shouldReturnEmptyWhenClientReturnsNull() {
        UUID programId = UUID.randomUUID();
        User trainer = user(UUID.randomUUID(), "trainer", UserRole.TRAINER);
        WorkoutProgram program = workoutProgram(programId, trainer);
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        allowUserForProgram(programId, "trainer", trainer, program);
        when(nutritionClient.createPlan(requestDto)).thenReturn(null);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(programId, requestDto, "trainer");

        assertThat(result).isEmpty();
    }

    @Test
    void createPlan_shouldReturnEmptyWhenFeignCallFails() {
        UUID programId = UUID.randomUUID();
        User trainer = user(UUID.randomUUID(), "trainer", UserRole.TRAINER);
        WorkoutProgram program = workoutProgram(programId, trainer);
        NutritionPlanRequestDto requestDto = nutritionPlanRequestDto(programId);
        FeignException exception = mock(FeignException.class);
        allowUserForProgram(programId, "trainer", trainer, program);
        when(nutritionClient.createPlan(requestDto)).thenThrow(exception);

        Optional<NutritionPlanResponseDto> result = nutritionIntegrationService.createPlan(programId, requestDto, "trainer");

        assertThat(result).isEmpty();
    }

    @Test
    void addMeal_shouldAllowTrainerWhenTrainerOwnsProgramAndPlanMatchesProgram() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User trainer = user(UUID.randomUUID(), "trainer", UserRole.TRAINER);
        WorkoutProgram program = workoutProgram(programId, trainer);
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        MealEntryResponseDto responseDto = mealEntryResponseDto(UUID.randomUUID());
        allowUserForProgram(programId, "trainer", trainer, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(nutritionPlanResponseDto(planId, programId));
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenReturn(responseDto);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "trainer");

        assertThat(result).containsSame(responseDto);
        verify(workoutProgramService, times(1)).verifyTrainerOwnership(program, trainer);
        verify(nutritionClient, times(1)).getPlanByProgramId(programId);
        verify(nutritionClient, times(1)).addMealToPlan(planId, requestDto);
    }

    @Test
    void addMeal_shouldAllowAdminWhenPlanMatchesProgram() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        MealEntryResponseDto responseDto = mealEntryResponseDto(UUID.randomUUID());
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(nutritionPlanResponseDto(planId, programId));
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenReturn(responseDto);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "admin");

        assertThat(result).containsSame(responseDto);
        verify(workoutProgramService, never()).verifyTrainerOwnership(any(WorkoutProgram.class), any(User.class));
        verify(nutritionClient, times(1)).getPlanByProgramId(programId);
        verify(nutritionClient, times(1)).addMealToPlan(planId, requestDto);
    }

    @Test
    void addMeal_shouldRejectTrainerWhenTrainerDoesNotOwnProgram() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User trainer = user(UUID.randomUUID(), "trainer", UserRole.TRAINER);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "owner", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        allowUserForProgram(programId, "trainer", trainer, program);
        doThrow(new UnauthorizedProgramAccessException("You can only manage your own workout programs"))
                .when(workoutProgramService)
                .verifyTrainerOwnership(program, trainer);

        assertThatThrownBy(() -> nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "trainer"))
                .isInstanceOf(UnauthorizedProgramAccessException.class)
                .hasMessage("You can only manage your own workout programs");

        verify(nutritionClient, never()).getPlanByProgramId(any());
        verify(nutritionClient, never()).addMealToPlan(any(), any());
    }

    @Test
    void addMeal_shouldRejectWhenPlanIdDoesNotMatchProgramPlan() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID returnedPlanId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(nutritionPlanResponseDto(returnedPlanId, programId));

        assertThatThrownBy(() -> nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "admin"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Nutrition plan does not match the requested workout program");

        verify(nutritionClient, times(1)).getPlanByProgramId(programId);
        verify(nutritionClient, never()).addMealToPlan(any(), any());
    }

    @Test
    void addMeal_shouldRejectWhenReturnedProgramIdDoesNotMatchRouteProgramId() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID returnedProgramId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(nutritionPlanResponseDto(planId, returnedProgramId));

        assertThatThrownBy(() -> nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "admin"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Nutrition plan does not match the requested workout program");

        verify(nutritionClient, times(1)).getPlanByProgramId(programId);
        verify(nutritionClient, never()).addMealToPlan(any(), any());
    }

    @Test
    void addMeal_shouldReturnEmptyWhenPlanCannotBeFound() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        FeignException.NotFound exception = mock(FeignException.NotFound.class);
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenThrow(exception);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "admin");

        assertThat(result).isEmpty();
        verify(nutritionClient, times(1)).getPlanByProgramId(programId);
        verify(nutritionClient, never()).addMealToPlan(any(), any());
    }

    @Test
    void addMeal_shouldReturnEmptyWhenClientReturnsNull() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(nutritionPlanResponseDto(planId, programId));
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenReturn(null);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "admin");

        assertThat(result).isEmpty();
    }

    @Test
    void addMeal_shouldReturnEmptyWhenFeignCallFails() {
        UUID programId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User admin = user(UUID.randomUUID(), "admin", UserRole.ADMIN);
        WorkoutProgram program = workoutProgram(programId, user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        MealEntryRequestDto requestDto = mealEntryRequestDto();
        FeignException exception = mock(FeignException.class);
        allowUserForProgram(programId, "admin", admin, program);
        when(nutritionClient.getPlanByProgramId(programId)).thenReturn(nutritionPlanResponseDto(planId, programId));
        when(nutritionClient.addMealToPlan(planId, requestDto)).thenThrow(exception);

        Optional<MealEntryResponseDto> result = nutritionIntegrationService.addMealToPlan(programId, planId, requestDto, "admin");

        assertThat(result).isEmpty();
    }

    private void allowUserForProgram(UUID programId, String username, User user, WorkoutProgram program) {
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(userService.findByUsername(username)).thenReturn(user);
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

    private static User user(UUID id, String username, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    private static WorkoutProgram workoutProgram(UUID id, User trainer) {
        WorkoutProgram program = new WorkoutProgram();
        program.setId(id);
        program.setTrainer(trainer);
        return program;
    }
}
