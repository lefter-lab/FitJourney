package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.dto.RegisterDto;
import com.fitjourney.fitjourney.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                          BindingResult result,
                          Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("passwordError", "Passwords do not match");
            return "auth/register";
        }

        try {
            userService.register(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
}

