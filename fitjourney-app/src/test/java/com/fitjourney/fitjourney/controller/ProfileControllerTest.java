package com.fitjourney.fitjourney.controller;

import com.fitjourney.fitjourney.config.SecurityConfig;
import com.fitjourney.fitjourney.dto.ProfileDto;
import com.fitjourney.fitjourney.security.CustomAuthenticationSuccessHandler;
import com.fitjourney.fitjourney.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Test
    @WithMockUser(username = "johnny")
    void showProfilePage_shouldReturnProfileViewForAuthenticatedUser() throws Exception {
        ProfileDto profileDto = profileDto("johnny", "john@example.com", "John", "Doe");
        when(userService.getProfile("johnny")).thenReturn(profileDto);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attribute("profileDto", sameInstance(profileDto)));

        verify(userService, times(1)).getProfile("johnny");
    }

    @Test
    void showProfilePage_shouldRedirectToLoginWhenUserIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(userService, never()).getProfile(any());
    }

    @Test
    @WithMockUser(username = "johnny")
    void updateProfile_shouldRedirectToProfileWhenUpdateSucceeds() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("username", "johnny")
                        .param("email", "new@example.com")
                        .param("firstName", "NewFirst")
                        .param("lastName", "NewLast"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        ArgumentCaptor<ProfileDto> captor = ArgumentCaptor.forClass(ProfileDto.class);
        verify(userService, times(1)).updateProfile(eq("johnny"), captor.capture());

        ProfileDto savedProfile = captor.getValue();
        assertThat(savedProfile.getUsername()).isEqualTo("johnny");
        assertThat(savedProfile.getEmail()).isEqualTo("new@example.com");
        assertThat(savedProfile.getFirstName()).isEqualTo("NewFirst");
        assertThat(savedProfile.getLastName()).isEqualTo("NewLast");
    }

    @Test
    @WithMockUser(username = "johnny")
    void updateProfile_shouldReturnProfileViewWhenValidationFails() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("username", "johnny")
                        .param("email", "invalid-email")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attributeHasFieldErrors(
                        "profileDto",
                        "email",
                        "firstName",
                        "lastName"));

        verify(userService, never()).updateProfile(any(), any(ProfileDto.class));
    }

    @Test
    @WithMockUser(username = "johnny")
    void updateProfile_shouldReturnProfileViewWhenServiceThrowsBusinessException() throws Exception {
        doThrow(new IllegalArgumentException("Email is already in use"))
                .when(userService)
                .updateProfile(eq("johnny"), any(ProfileDto.class));

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("username", "johnny")
                        .param("email", "used@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attributeHasFieldErrors("profileDto", "email"));

        verify(userService, times(1)).updateProfile(eq("johnny"), any(ProfileDto.class));
    }

    private static ProfileDto profileDto(String username, String email, String firstName, String lastName) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setUsername(username);
        profileDto.setEmail(email);
        profileDto.setFirstName(firstName);
        profileDto.setLastName(lastName);
        return profileDto;
    }
}
