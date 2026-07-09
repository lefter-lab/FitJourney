package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.dto.EnrollmentProgressDto;
import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.exception.DuplicateEnrollmentException;
import com.fitjourney.fitjourney.service.EnrollmentService;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
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
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final WorkoutProgramService workoutProgramService;
    private final UserService userService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{programId}/enroll")
    public String enrollInProgram(@PathVariable UUID programId,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        WorkoutProgram program = workoutProgramService.findById(programId);

        try {
            enrollmentService.enrollUser(user, program);
        } catch (DuplicateEnrollmentException exception) {
            redirectAttributes.addFlashAttribute("enrollmentError", exception.getMessage());
            return "redirect:/programs/all";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Successfully enrolled in the program");
        return "redirect:/programs/all";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public String myEnrollments(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByUser(user);
        model.addAttribute("enrollments", enrollments);
        return "enrollments/my-enrollments";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{enrollmentId}/progress")
    public String updateProgress(@PathVariable UUID enrollmentId,
                                 @Valid @ModelAttribute("progressDto") EnrollmentProgressDto progressDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 Principal principal) {
        if (bindingResult.hasErrors()) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("enrollments", enrollmentService.getEnrollmentsByUser(user));
            model.addAttribute("progressEnrollmentId", enrollmentId);
            model.addAttribute("progressDto", progressDto);
            if (bindingResult.getFieldError("percentage") != null) {
                model.addAttribute("progressError", bindingResult.getFieldError("percentage").getDefaultMessage());
            }
            return "enrollments/my-enrollments";
        }

        enrollmentService.updateProgress(enrollmentId, progressDto.getPercentage());
        return "redirect:/enrollments/my";
    }
}
