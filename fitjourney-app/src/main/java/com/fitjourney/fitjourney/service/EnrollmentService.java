package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.EnrollmentStatus;
import com.fitjourney.fitjourney.exception.DuplicateEnrollmentException;
import com.fitjourney.fitjourney.exception.EnrollmentNotFoundException;
import com.fitjourney.fitjourney.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public void enrollUser(User user, WorkoutProgram program) {
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndWorkoutProgramId(
                user.getId(),
                program.getId()
        );

        if (alreadyEnrolled) {
            throw new DuplicateEnrollmentException("You are already enrolled in this program");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setWorkoutProgram(program);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercentage(0);

        enrollmentRepository.save(enrollment);
        log.info("User '{}' successfully enrolled in workout program with ID '{}'.", user.getUsername(), program.getId());
    }

    public List<Enrollment> getEnrollmentsByUser(User user) {
        return enrollmentRepository.findAllByUserId(user.getId());
    }

    public void updateProgress(UUID enrollmentId, int percentage) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found"));

        enrollment.setProgressPercentage(percentage);
        enrollmentRepository.save(enrollment);
    }

    public void updateCompletedEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();

        enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .filter(e -> e.getProgressPercentage() >= 100)
                .forEach(e -> {
                    e.setStatus(EnrollmentStatus.COMPLETED);
                    enrollmentRepository.save(e);
                    log.info("Enrollment with ID '{}' was marked as completed.", e.getId());
                });
    }

    public void expireOverdueEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .filter(e -> e.getProgressPercentage() < 100)
                .filter(e -> e.getEnrolledAt().plusWeeks(e.getWorkoutProgram().getDurationWeeks()).isBefore(now))
                .forEach(e -> {
                    e.setStatus(EnrollmentStatus.EXPIRED);
                    enrollmentRepository.save(e);
                    log.info("Enrollment with ID '{}' was marked as expired.", e.getId());
                });
    }
}
