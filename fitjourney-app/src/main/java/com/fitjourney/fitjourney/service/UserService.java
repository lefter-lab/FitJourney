package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.dto.RegisterDto;
import com.fitjourney.fitjourney.dto.ProfileDto;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.enums.UserRole;
import com.fitjourney.fitjourney.exception.UserNotFoundException;
import com.fitjourney.fitjourney.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.USER);

        userRepository.save(user);
        log.info("Successfully registered new user with username: {}", dto.getUsername());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public ProfileDto getProfile(String username) {
        User user = findByUsername(username);

        ProfileDto profileDto = new ProfileDto();
        profileDto.setUsername(user.getUsername());
        profileDto.setEmail(user.getEmail());
        profileDto.setFirstName(user.getFirstName());
        profileDto.setLastName(user.getLastName());
        return profileDto;
    }

    public void updateProfile(String currentUsername, ProfileDto profileDto) {
        User currentUser = findByUsername(currentUsername);

        userRepository.findByEmail(profileDto.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(currentUser.getId()))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("Email is already in use");
                });

        currentUser.setEmail(profileDto.getEmail());
        currentUser.setFirstName(profileDto.getFirstName());
        currentUser.setLastName(profileDto.getLastName());

        userRepository.save(currentUser);
        log.info("User profile updated successfully for username: {}", currentUsername);
    }

    public void changeRole(UUID id, UserRole role) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}

