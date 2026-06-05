package com.fitjourney.fitjourney.service;

import com.fitjourney.fitjourney.entity.Enrollment;
import com.fitjourney.fitjourney.entity.User;
import com.fitjourney.fitjourney.entity.WorkoutProgram;
import com.fitjourney.fitjourney.enums.EnrollmentStatus;
import com.fitjourney.fitjourney.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public void enrollUser(User user, WorkoutProgram program) {
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndWorkoutProgramId(
                user.getId(),
                program.getId()
        );

        if (alreadyEnrolled) {
            throw new IllegalArgumentException("User is already enrolled in this program");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setWorkoutProgram(program);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercentage(0);

        enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getEnrollmentsByUser(User user) {
        return enrollmentRepository.findAllByUserId(user.getId());
    }

    public void updateProgress(UUID enrollmentId, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        enrollment.setProgressPercentage(percentage);
        enrollmentRepository.save(enrollment);
    }

    public void updateExpiredEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();

        enrollments.stream()
                .filter(e -> e.getProgressPercentage() >= 100)
                .forEach(e -> {
                    e.setStatus(EnrollmentStatus.COMPLETED);
                    enrollmentRepository.save(e);
                });
    }
}