package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.service.UserService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/programs")
public class WorkoutProgramController {

    private final WorkoutProgramService workoutProgramService;
    private final UserService userService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("programDto", new WorkoutProgramDto());
        model.addAttribute("levels", DifficultyLevel.values());
        return "programs/program-create";
    }

    @PostMapping("/create")
    public String createProgram(@Valid @ModelAttribute("programDto") WorkoutProgramDto dto,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("levels", DifficultyLevel.values());
            return "programs/program-create";
        }

        var user = userService.findByUsername(principal.getName());
        workoutProgramService.createProgram(dto, user);

        return "redirect:/dashboard";
    }
}

