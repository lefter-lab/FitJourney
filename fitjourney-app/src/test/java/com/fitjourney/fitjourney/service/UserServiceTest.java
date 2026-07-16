package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.ProfileDto;
import com.fitjourney.fitjourney.dto.RegisterDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.enums.UserRole;
import com.fitjourney.fitjourney.exception.UserNotFoundException;
import com.fitjourney.fitjourney.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldSaveEncodedUserAndDefaultRole() {
        RegisterDto dto = registerDto("johnny", "john@example.com", "secret123", "John", "Doe");
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-secret123");

        userService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        verify(passwordEncoder, times(1)).encode(dto.getPassword());

        User savedUser = captor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(dto.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(dto.getEmail());
        assertThat(savedUser.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(dto.getLastName());
        assertThat(savedUser.getPassword()).isEqualTo("encoded-secret123");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterDto dto = registerDto("johnny", "john@example.com", "secret123", "John", "Doe");
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");

        verify(passwordEncoder, never()).encode(dto.getPassword());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        RegisterDto dto = registerDto("johnny", "john@example.com", "secret123", "John", "Doe");
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(passwordEncoder, never()).encode(dto.getPassword());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void findByUsername_shouldReturnUserWhenFound() {
        User user = user(UUID.randomUUID(), "johnny", "john@example.com", "John", "Doe", UserRole.USER);
        when(userRepository.findByUsername("johnny")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("johnny");

        assertThat(result).isSameAs(user);
    }

    @Test
    void findByUsername_shouldThrowWhenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("missing"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: missing");
    }

    @Test
    void getProfile_shouldMapUserToProfileDto() {
        User user = user(UUID.randomUUID(), "johnny", "john@example.com", "John", "Doe", UserRole.TRAINER);
        when(userRepository.findByUsername("johnny")).thenReturn(Optional.of(user));

        ProfileDto result = userService.getProfile("johnny");

        assertThat(result.getUsername()).isEqualTo("johnny");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
    }

    @Test
    void updateProfile_shouldUpdateAllowedFieldsAndSave() {
        UUID userId = UUID.randomUUID();
        User currentUser = user(userId, "johnny", "old@example.com", "OldFirst", "OldLast", UserRole.TRAINER);
        ProfileDto profileDto = profileDto("johnny", "new@example.com", "NewFirst", "NewLast");

        when(userRepository.findByUsername("johnny")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(profileDto.getEmail())).thenReturn(Optional.empty());

        userService.updateProfile("johnny", profileDto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("johnny");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("NewFirst");
        assertThat(savedUser.getLastName()).isEqualTo("NewLast");
        assertThat(savedUser.getPassword()).isEqualTo("old-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.TRAINER);
    }

    @Test
    void updateProfile_shouldThrowWhenEmailUsedByAnotherUser() {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "johnny", "old@example.com", "OldFirst", "OldLast", UserRole.USER);
        User otherUser = user(otherUserId, "other", "new@example.com", "OtherFirst", "OtherLast", UserRole.ADMIN);
        ProfileDto profileDto = profileDto("johnny", "new@example.com", "NewFirst", "NewLast");

        when(userRepository.findByUsername("johnny")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(profileDto.getEmail())).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> userService.updateProfile("johnny", profileDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void changeRole_shouldUpdateRoleAndSave() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "johnny", "john@example.com", "John", "Doe", UserRole.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId, UserRole.ADMIN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeRole_shouldThrowWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeRole(userId, UserRole.ADMIN))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    private static RegisterDto registerDto(String username, String email, String password, String firstName, String lastName) {
        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setConfirmPassword(password);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        return dto;
    }

    private static ProfileDto profileDto(String username, String email, String firstName, String lastName) {
        ProfileDto dto = new ProfileDto();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        return dto;
    }

    private static User user(UUID id, String username, String email, String firstName, String lastName, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("old-password");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }
}
