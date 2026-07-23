package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.config.SecurityConfig;
import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.enums.EnrollmentStatus;
import com.fitjourney.fitjourney.enums.UserRole;
import com.fitjourney.fitjourney.exception.DuplicateEnrollmentException;
import com.fitjourney.fitjourney.exception.EnrollmentNotFoundException;
import com.fitjourney.fitjourney.exception.ProgramNotFoundException;
import com.fitjourney.fitjourney.exception.UserNotFoundException;
import com.fitjourney.fitjourney.security.CustomAuthenticationSuccessHandler;
import com.fitjourney.fitjourney.service.EnrollmentService;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfig.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private WorkoutProgramService workoutProgramService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void enrollInProgram_shouldRedirectWithSuccessMessageWhenUserEnrolls() throws Exception {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        WorkoutProgram program = workoutProgram(programId);
        when(userService.findByUsername("johnny")).thenReturn(user);
        when(workoutProgramService.findById(programId)).thenReturn(program);

        mockMvc.perform(post("/enrollments/{programId}/enroll", programId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"))
                .andExpect(flash().attribute("successMessage", "Successfully enrolled in the program"));

        verify(userService, times(1)).findByUsername("johnny");
        verify(workoutProgramService, times(1)).findById(programId);
        verify(enrollmentService, times(1)).enrollUser(user, program);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void enrollInProgram_shouldRedirectWithEnrollmentErrorWhenDuplicateEnrollmentOccurs() throws Exception {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        WorkoutProgram program = workoutProgram(programId);
        when(userService.findByUsername("johnny")).thenReturn(user);
        when(workoutProgramService.findById(programId)).thenReturn(program);
        doThrow(new DuplicateEnrollmentException("You are already enrolled in this program"))
                .when(enrollmentService)
                .enrollUser(user, program);

        mockMvc.perform(post("/enrollments/{programId}/enroll", programId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/all"))
                .andExpect(flash().attribute("enrollmentError", "You are already enrolled in this program"));

        verify(enrollmentService, times(1)).enrollUser(user, program);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void enrollInProgram_shouldReturnNotFoundWhenProgramIsMissing() throws Exception {
        UUID programId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        when(userService.findByUsername("johnny")).thenReturn(user);
        when(workoutProgramService.findById(programId))
                .thenThrow(new ProgramNotFoundException("Program not found with id: " + programId));

        mockMvc.perform(post("/enrollments/{programId}/enroll", programId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "Program not found with id: " + programId));

        verify(enrollmentService, never()).enrollUser(any(User.class), any(WorkoutProgram.class));
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void enrollInProgram_shouldReturnNotFoundWhenUserIsMissing() throws Exception {
        UUID programId = UUID.randomUUID();
        when(userService.findByUsername("johnny")).thenThrow(new UserNotFoundException("User not found: johnny"));

        mockMvc.perform(post("/enrollments/{programId}/enroll", programId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "User not found: johnny"));

        verify(workoutProgramService, never()).findById(any());
        verify(enrollmentService, never()).enrollUser(any(User.class), any(WorkoutProgram.class));
    }

    @Test
    void enrollInProgram_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/enrollments/{programId}/enroll", UUID.randomUUID()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(enrollmentService, never()).enrollUser(any(User.class), any(WorkoutProgram.class));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void enrollInProgram_shouldBeForbiddenForTrainer() throws Exception {
        mockMvc.perform(post("/enrollments/{programId}/enroll", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));

        verify(enrollmentService, never()).enrollUser(any(User.class), any(WorkoutProgram.class));
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void myEnrollments_shouldReturnMyEnrollmentsViewForAuthenticatedUser() throws Exception {
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        List<Enrollment> enrollments = List.of(enrollment(UUID.randomUUID(), user, workoutProgram(UUID.randomUUID())));
        when(userService.findByUsername("johnny")).thenReturn(user);
        when(enrollmentService.getEnrollmentsByUser(user)).thenReturn(enrollments);

        mockMvc.perform(get("/enrollments/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollments/my-enrollments"))
                .andExpect(model().attribute("enrollments", sameInstance(enrollments)));

        verify(userService, times(1)).findByUsername("johnny");
        verify(enrollmentService, times(1)).getEnrollmentsByUser(user);
    }

    @Test
    void myEnrollments_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/enrollments/my"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(enrollmentService, never()).getEnrollmentsByUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void myEnrollments_shouldBeForbiddenForTrainer() throws Exception {
        mockMvc.perform(get("/enrollments/my"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));

        verify(enrollmentService, never()).getEnrollmentsByUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void updateProgress_shouldRedirectToMyEnrollmentsWhenProgressIsValid() throws Exception {
        UUID enrollmentId = UUID.randomUUID();

        mockMvc.perform(post("/enrollments/{enrollmentId}/progress", enrollmentId)
                        .with(csrf())
                        .param("percentage", "75"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/enrollments/my"));

        verify(enrollmentService, times(1)).updateProgress(enrollmentId, 75);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void updateProgress_shouldReturnMyEnrollmentsViewWhenProgressIsInvalid() throws Exception {
        UUID enrollmentId = UUID.randomUUID();
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        List<Enrollment> enrollments = List.of(enrollment(enrollmentId, user, workoutProgram(UUID.randomUUID())));
        when(userService.findByUsername("johnny")).thenReturn(user);
        when(enrollmentService.getEnrollmentsByUser(user)).thenReturn(enrollments);

        mockMvc.perform(post("/enrollments/{enrollmentId}/progress", enrollmentId)
                        .with(csrf())
                        .param("percentage", "101"))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollments/my-enrollments"))
                .andExpect(model().attribute("enrollments", sameInstance(enrollments)))
                .andExpect(model().attribute("progressEnrollmentId", enrollmentId))
                .andExpect(model().attributeExists("progressDto"))
                .andExpect(model().attribute("progressError", "Progress must be at most 100"))
                .andExpect(model().attributeHasFieldErrors("progressDto", "percentage"));

        verify(enrollmentService, never()).updateProgress(any(), anyInt());
        verify(userService, times(1)).findByUsername("johnny");
        verify(enrollmentService, times(1)).getEnrollmentsByUser(user);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void updateProgress_shouldReturnNotFoundWhenEnrollmentIsMissing() throws Exception {
        UUID enrollmentId = UUID.randomUUID();
        doThrow(new EnrollmentNotFoundException("Enrollment not found"))
                .when(enrollmentService)
                .updateProgress(enrollmentId, 50);

        mockMvc.perform(post("/enrollments/{enrollmentId}/progress", enrollmentId)
                        .with(csrf())
                        .param("percentage", "50"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "Enrollment not found"));
    }

    @Test
    void updateProgress_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/enrollments/{enrollmentId}/progress", UUID.randomUUID())
                        .with(csrf())
                        .param("percentage", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(enrollmentService, never()).updateProgress(any(), anyInt());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void updateProgress_shouldBeForbiddenForTrainer() throws Exception {
        mockMvc.perform(post("/enrollments/{enrollmentId}/progress", UUID.randomUUID())
                        .with(csrf())
                        .param("percentage", "50"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));

        verify(enrollmentService, never()).updateProgress(any(), anyInt());
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

    private static WorkoutProgram workoutProgram(UUID id) {
        WorkoutProgram program = new WorkoutProgram();
        program.setId(id);
        program.setTitle("Strength Plan");
        program.setDescription("Complete strength program");
        program.setDifficulty(DifficultyLevel.INTERMEDIATE);
        program.setDurationWeeks(8);
        program.setPrice(BigDecimal.valueOf(49.99));
        program.setActive(true);
        program.setTrainer(user(UUID.randomUUID(), "trainer", UserRole.TRAINER));
        return program;
    }

    private static Enrollment enrollment(UUID id, User user, WorkoutProgram program) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(id);
        enrollment.setUser(user);
        enrollment.setWorkoutProgram(program);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercentage(30);
        return enrollment;
    }
}
