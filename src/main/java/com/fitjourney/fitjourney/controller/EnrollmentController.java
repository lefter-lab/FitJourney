package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.service.EnrollmentService;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
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
}

