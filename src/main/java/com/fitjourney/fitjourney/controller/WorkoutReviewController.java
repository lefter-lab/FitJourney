package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.dto.WorkoutReviewDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.exception.DuplicateReviewException;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import com.fitjourney.fitjourney.service.WorkoutReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class WorkoutReviewController {

    private final WorkoutReviewService workoutReviewService;
    private final WorkoutProgramService workoutProgramService;
    private final UserService userService;

    @GetMapping("/programs/{programId}")
    public String showProgramReviews(@PathVariable UUID programId, Model model) {
        WorkoutProgram program = workoutProgramService.findById(programId);

        model.addAttribute("program", program);
        model.addAttribute("reviews", workoutReviewService.getReviewsForProgram(programId));
        model.addAttribute("reviewDto", new WorkoutReviewDto());

        return "reviews/program-reviews";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/programs/{programId}")
    public String createReview(@PathVariable UUID programId,
                               @Valid @ModelAttribute("reviewDto") WorkoutReviewDto reviewDto,
                               BindingResult bindingResult,
                               Model model,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        WorkoutProgram program = workoutProgramService.findById(programId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("program", program);
            model.addAttribute("reviews", workoutReviewService.getReviewsForProgram(programId));
            return "reviews/program-reviews";
        }

        User user = userService.findByUsername(principal.getName());

        try {
            workoutReviewService.createReview(user, program, reviewDto);
        } catch (DuplicateReviewException exception) {
            model.addAttribute("program", program);
            model.addAttribute("reviews", workoutReviewService.getReviewsForProgram(programId));
            model.addAttribute("reviewError", exception.getMessage());
            return "reviews/program-reviews";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully");
        return "redirect:/reviews/programs/" + programId;
    }
}
