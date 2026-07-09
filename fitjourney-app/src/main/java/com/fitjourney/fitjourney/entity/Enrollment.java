package com.fitjourney.fitjourney.entity;

import com.fitjourney.fitjourney.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private WorkoutProgram workoutProgram;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "progress_percentage", nullable = false)
    private int progressPercentage = 0;
}

