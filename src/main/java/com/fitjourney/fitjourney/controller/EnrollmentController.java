package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.service.EnrollmentService;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/{programId}/enroll")
    public String enrollInProgram(@PathVariable UUID programId, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        WorkoutProgram program = workoutProgramService.findById(programId);
        enrollmentService.enrollUser(user, program);
        return "redirect:/programs/all";
    }

    @GetMapping("/my")
    public String myEnrollments(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByUser(user);
        model.addAttribute("enrollments", enrollments);
        return "enrollments/my-enrollments";
    }

    @PostMapping("/{enrollmentId}/progress")
    public String updateProgress(@PathVariable UUID enrollmentId, @RequestParam int percentage) {
        enrollmentService.updateProgress(enrollmentId, percentage);
        return "redirect:/enrollments/my";
    }
}
