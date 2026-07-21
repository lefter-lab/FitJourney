package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.config.SecurityConfig;
import com.fitjourney.fitjourney.dto.RegisterDto;
import com.fitjourney.fitjourney.security.CustomAuthenticationSuccessHandler;
import com.fitjourney.fitjourney.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Test
    void showLoginForm_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void showRegisterForm_shouldReturnRegisterViewWithFormModel() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("registerDto", instanceOf(RegisterDto.class)));
    }

    @Test
    void register_shouldRedirectToLoginWhenRegistrationSucceeds() throws Exception {
        doNothing().when(userService).register(any(RegisterDto.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "johnny")
                        .param("email", "john@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).register(any(RegisterDto.class));
    }

    @Test
    void register_shouldReturnRegisterViewWhenValidationFails() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "")
                        .param("email", "invalid-email")
                        .param("password", "123")
                        .param("confirmPassword", "")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors(
                        "registerDto",
                        "username",
                        "email",
                        "password",
                        "confirmPassword",
                        "firstName",
                        "lastName"));

        verify(userService, never()).register(any(RegisterDto.class));
    }

    @Test
    void register_shouldReturnRegisterViewWhenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "johnny")
                        .param("email", "john@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "different123")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("passwordError", "Passwords do not match"));

        verify(userService, never()).register(any(RegisterDto.class));
    }

    @Test
    void register_shouldReturnRegisterViewWhenServiceThrowsBusinessException() throws Exception {
        doThrow(new IllegalArgumentException("Username already exists"))
                .when(userService)
                .register(any(RegisterDto.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "johnny")
                        .param("email", "john@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("error", "Username already exists"));

        verify(userService, times(1)).register(any(RegisterDto.class));
    }
}
