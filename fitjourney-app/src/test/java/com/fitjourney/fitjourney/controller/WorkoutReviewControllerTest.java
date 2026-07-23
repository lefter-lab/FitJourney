package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.config.SecurityConfig;
import com.fitjourney.fitjourney.dto.WorkoutReviewDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.entity.WorkoutReview;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.enums.UserRole;
import com.fitjourney.fitjourney.exception.DuplicateReviewException;
import com.fitjourney.fitjourney.exception.ProgramNotFoundException;
import com.fitjourney.fitjourney.exception.UserNotFoundException;
import com.fitjourney.fitjourney.security.CustomAuthenticationSuccessHandler;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import com.fitjourney.fitjourney.service.WorkoutReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(WorkoutReviewController.class)
@Import(SecurityConfig.class)
class WorkoutReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutReviewService workoutReviewService;

    @MockitoBean
    private WorkoutProgramService workoutProgramService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Test
    @WithMockUser
    void showProgramReviews_shouldReturnReviewsViewForAuthenticatedUser() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        List<WorkoutReview> reviews = List.of(workoutReview(UUID.randomUUID(), program));
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(workoutReviewService.getReviewsForProgram(programId)).thenReturn(reviews);

        mockMvc.perform(get("/reviews/programs/{programId}", programId))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/program-reviews"))
                .andExpect(model().attribute("program", sameInstance(program)))
                .andExpect(model().attribute("reviews", sameInstance(reviews)))
                .andExpect(model().attribute("reviewDto", instanceOf(WorkoutReviewDto.class)));

        verify(workoutProgramService, times(1)).findById(programId);
        verify(workoutReviewService, times(1)).getReviewsForProgram(programId);
    }

    @Test
    void showProgramReviews_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/reviews/programs/{programId}", UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(workoutProgramService, never()).findById(any());
        verify(workoutReviewService, never()).getReviewsForProgram(any());
    }

    @Test
    @WithMockUser
    void showProgramReviews_shouldReturnNotFoundWhenProgramIsMissing() throws Exception {
        UUID programId = UUID.randomUUID();
        when(workoutProgramService.findById(programId))
                .thenThrow(new ProgramNotFoundException("Program not found with id: " + programId));

        mockMvc.perform(get("/reviews/programs/{programId}", programId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "Program not found with id: " + programId));

        verify(workoutReviewService, never()).getReviewsForProgram(any());
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldRedirectWithSuccessMessageWhenReviewIsSubmitted() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(userService.findByUsername("johnny")).thenReturn(user);

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/programs/" + programId))
                .andExpect(flash().attribute("successMessage", "Review submitted successfully"));

        ArgumentCaptor<WorkoutReviewDto> captor = ArgumentCaptor.forClass(WorkoutReviewDto.class);
        verify(workoutProgramService, times(1)).findById(programId);
        verify(userService, times(1)).findByUsername("johnny");
        verify(workoutReviewService, times(1)).addReview(captor.capture(), eq(user));
        assertThat(captor.getValue().getProgramId()).isEqualTo(programId);
        assertThat(captor.getValue().getRating()).isEqualTo(5);
        assertThat(captor.getValue().getComment()).isEqualTo("Excellent program");
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldReturnReviewsViewWhenRatingIsOutOfRange() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        List<WorkoutReview> reviews = List.of(workoutReview(UUID.randomUUID(), program));
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(workoutReviewService.getReviewsForProgram(programId)).thenReturn(reviews);

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "6")
                        .param("comment", "Excellent program"))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/program-reviews"))
                .andExpect(model().attribute("program", sameInstance(program)))
                .andExpect(model().attribute("reviews", sameInstance(reviews)))
                .andExpect(model().attributeHasFieldErrors("reviewDto", "rating"));

        verify(userService, never()).findByUsername(any());
        verify(workoutReviewService, never()).addReview(any(WorkoutReviewDto.class), any(User.class));
        verify(workoutReviewService, times(1)).getReviewsForProgram(programId);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldReturnReviewsViewWhenCommentIsTooLong() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        List<WorkoutReview> reviews = List.of(workoutReview(UUID.randomUUID(), program));
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(workoutReviewService.getReviewsForProgram(programId)).thenReturn(reviews);

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "4")
                        .param("comment", "a".repeat(501)))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/program-reviews"))
                .andExpect(model().attribute("program", sameInstance(program)))
                .andExpect(model().attribute("reviews", sameInstance(reviews)))
                .andExpect(model().attributeHasFieldErrors("reviewDto", "comment"));

        verify(userService, never()).findByUsername(any());
        verify(workoutReviewService, never()).addReview(any(WorkoutReviewDto.class), any(User.class));
        verify(workoutReviewService, times(1)).getReviewsForProgram(programId);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldReturnReviewsViewWithReviewErrorWhenDuplicateReviewOccurs() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        List<WorkoutReview> reviews = List.of(workoutReview(UUID.randomUUID(), program));
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(userService.findByUsername("johnny")).thenReturn(user);
        when(workoutReviewService.getReviewsForProgram(programId)).thenReturn(reviews);
        doThrow(new DuplicateReviewException("You have already reviewed this program"))
                .when(workoutReviewService)
                .addReview(any(WorkoutReviewDto.class), eq(user));

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/program-reviews"))
                .andExpect(model().attribute("program", sameInstance(program)))
                .andExpect(model().attribute("reviews", sameInstance(reviews)))
                .andExpect(model().attribute("reviewError", "You have already reviewed this program"));

        verify(workoutReviewService, times(1)).addReview(any(WorkoutReviewDto.class), eq(user));
        verify(workoutReviewService, times(1)).getReviewsForProgram(programId);
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldReturnNotFoundWhenProgramLookupFails() throws Exception {
        UUID programId = UUID.randomUUID();
        when(workoutProgramService.findById(programId))
                .thenThrow(new ProgramNotFoundException("Program not found with id: " + programId));

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "Program not found with id: " + programId));

        verify(userService, never()).findByUsername(any());
        verify(workoutReviewService, never()).addReview(any(WorkoutReviewDto.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldReturnNotFoundWhenUserLookupFails() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(userService.findByUsername("johnny")).thenThrow(new UserNotFoundException("User not found: johnny"));

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "User not found: johnny"));

        verify(workoutReviewService, never()).addReview(any(WorkoutReviewDto.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "johnny", roles = "USER")
    void createReview_shouldReturnNotFoundWhenReviewServiceProgramLookupFails() throws Exception {
        UUID programId = UUID.randomUUID();
        WorkoutProgram program = workoutProgram(programId);
        User user = user(UUID.randomUUID(), "johnny", UserRole.USER);
        when(workoutProgramService.findById(programId)).thenReturn(program);
        when(userService.findByUsername("johnny")).thenReturn(user);
        doThrow(new ProgramNotFoundException("Program not found"))
                .when(workoutReviewService)
                .addReview(any(WorkoutReviewDto.class), eq(user));

        mockMvc.perform(post("/reviews/programs/{programId}", programId)
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "Program not found"));

        verify(workoutReviewService, times(1)).addReview(any(WorkoutReviewDto.class), eq(user));
    }

    @Test
    void createReview_shouldRedirectToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/reviews/programs/{programId}", UUID.randomUUID())
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(workoutReviewService, never()).addReview(any(WorkoutReviewDto.class), any(User.class));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void createReview_shouldBeForbiddenForTrainer() throws Exception {
        mockMvc.perform(post("/reviews/programs/{programId}", UUID.randomUUID())
                        .with(csrf())
                        .param("rating", "5")
                        .param("comment", "Excellent program"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));

        verify(workoutReviewService, never()).addReview(any(WorkoutReviewDto.class), any(User.class));
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

    private static WorkoutReview workoutReview(UUID id, WorkoutProgram program) {
        WorkoutReview review = new WorkoutReview();
        review.setId(id);
        review.setUser(user(UUID.randomUUID(), "johnny", UserRole.USER));
        review.setWorkoutProgram(program);
        review.setRating(5);
        review.setComment("Excellent program");
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }
}
