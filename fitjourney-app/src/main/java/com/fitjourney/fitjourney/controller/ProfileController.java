package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.dto.ProfileDto;
import com.fitjourney.fitjourney.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String showProfilePage(Principal principal, Model model) {
        model.addAttribute("profileDto", userService.getProfile(principal.getName()));
        return "profile/profile";
    }

    @PostMapping
    public String updateProfile(Principal principal,
                                @Valid @ModelAttribute("profileDto") ProfileDto profileDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        profileDto.setUsername(principal.getName());

        if (bindingResult.hasErrors()) {
            profileDto.setUsername(principal.getName());
            return "profile/profile";
        }

        try {
            userService.updateProfile(principal.getName(), profileDto);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("email", "email.exists", exception.getMessage());
            profileDto.setUsername(principal.getName());
            return "profile/profile";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/profile";
    }
}
