package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.client.dto.MealEntryRequestDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanRequestDto;
import com.fitjourney.fitjourney.dto.WorkoutProgramDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.DifficultyLevel;
import com.fitjourney.fitjourney.exception.UnauthorizedProgramAccessException;
import com.fitjourney.fitjourney.service.NutritionIntegrationService;
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
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/programs")
public class WorkoutProgramController {

    private final WorkoutProgramService workoutProgramService;
    private final UserService userService;
    private final NutritionIntegrationService nutritionIntegrationService;

    @GetMapping("/{id}")
    public String showProgramDetails(@PathVariable UUID id, Model model) {
        WorkoutProgram program = workoutProgramService.findById(id);
        NutritionPlanRequestDto nutritionPlanForm = new NutritionPlanRequestDto();
        nutritionPlanForm.setProgramId(id);
        model.addAttribute("mealEntryForm", new MealEntryRequestDto());
        model.addAttribute("program", program);
        model.addAttribute("nutritionPlan", nutritionIntegrationService.findPlanByProgramId(id).orElse(null));
        model.addAttribute("nutritionPlanForm", nutritionPlanForm);
        return "programs/program-details";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/{id}/nutrition-plan")
    public String createNutritionPlan(@PathVariable UUID id,
                                      @Valid @ModelAttribute("nutritionPlanForm") NutritionPlanRequestDto nutritionPlanForm,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        nutritionPlanForm.setProgramId(id);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("nutritionPlanError", "Please correct the nutrition plan form.");
            return "redirect:/programs/" + id;
        }

        if (nutritionIntegrationService.createPlan(nutritionPlanForm).isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Nutrition plan created successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Nutrition plan could not be created right now.");
        }

        return "redirect:/programs/" + id;
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/{programId}/nutrition-plan/{planId}/meals")
    public String addMealToNutritionPlan(@PathVariable UUID programId,
                                         @PathVariable UUID planId,
                                         @Valid @ModelAttribute("mealEntryForm") MealEntryRequestDto mealEntryForm,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the meal form.");
            return "redirect:/programs/" + programId;
        }

        if (nutritionIntegrationService.addMealToPlan(planId, mealEntryForm).isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Meal added successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Meal could not be added right now.");
        }

        return "redirect:/programs/" + programId;
    }

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

        User user = userService.findByUsername(principal.getName());
        workoutProgramService.createProgram(dto, user);

        return "redirect:/programs/all";
    }

    @GetMapping("/all")
    public String getAllPrograms(Model model, Principal principal) {
        model.addAttribute("programs", workoutProgramService.getAllPrograms());
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("currentUserId", user.getId());
        }
        return "programs/programs-all";
    }

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model, Principal principal) {
        WorkoutProgram program = workoutProgramService.findById(id);
        User trainer = userService.findByUsername(principal.getName());
        workoutProgramService.verifyTrainerOwnership(program, trainer);

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
                              Model model,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("levels", DifficultyLevel.values());
            model.addAttribute("programId", id);
            return "programs/program-edit";
        }

        User trainer = userService.findByUsername(principal.getName());

        try {
            workoutProgramService.updateProgram(id, dto, trainer);
        } catch (UnauthorizedProgramAccessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/programs/all";
        }

        return "redirect:/programs/all";
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{id}/deactivate")
    public String deactivateProgram(@PathVariable UUID id,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        User trainer = userService.findByUsername(principal.getName());

        try {
            workoutProgramService.deactivateProgram(id, trainer);
        } catch (UnauthorizedProgramAccessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/programs/all";
        }

        return "redirect:/programs/all";
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{id}/delete")
    public String deleteProgram(@PathVariable UUID id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        User trainer = userService.findByUsername(principal.getName());

        try {
            workoutProgramService.deleteProgram(id, trainer);
        } catch (UnauthorizedProgramAccessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/programs/all";
        }

        return "redirect:/programs/all";
    }
}
