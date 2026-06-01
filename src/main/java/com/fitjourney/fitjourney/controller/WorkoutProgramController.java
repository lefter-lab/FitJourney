package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/programs")
public class WorkoutProgramController {

    private final WorkoutProgramService workoutProgramService;
    private final UserService userService;

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("programDto", new WorkoutProgramDto());
        model.addAttribute("levels", DifficultyLevel.values());
        return "programs/program-create";
    }

    @PreAuthorize("hasRole('TRAINER')")
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

        return "redirect:/programs/all";
    }

    @GetMapping("/all")
    public String getAllPrograms(Model model) {
        model.addAttribute("programs", workoutProgramService.getAllPrograms());
        return "programs/programs-all";
    }

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        WorkoutProgram program = workoutProgramService.findById(id);
        WorkoutProgramDto dto = new WorkoutProgramDto();
        dto.setName(program.getTitle());
        dto.setDescription(program.getDescription());
        dto.setDifficultyLevel(program.getDifficulty());
        dto.setDurationWeeks(program.getDurationWeeks());
        dto.setPrice(program.getPrice().doubleValue());

        model.addAttribute("programDto", dto);
        model.addAttribute("levels", DifficultyLevel.values());
        model.addAttribute("programId", program.getId());

        return "programs/program-edit";
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{id}/edit")
    public String editProgram(@PathVariable UUID id,
                              @Valid @ModelAttribute("programDto") WorkoutProgramDto dto,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("levels", DifficultyLevel.values());
            model.addAttribute("programId", id);
            return "programs/program-edit";
        }

        workoutProgramService.updateProgram(id, dto);
        return "redirect:/programs/all";
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{id}/deactivate")
    public String deactivateProgram(@PathVariable UUID id) {
        workoutProgramService.deactivateProgram(id);
        return "redirect:/programs/all";
    }
}
