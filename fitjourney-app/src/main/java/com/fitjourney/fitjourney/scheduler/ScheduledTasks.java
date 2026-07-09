package com.fitjourney.fitjourney.scheduler;

import com.fitjourney.fitjourney.service.EnrollmentService;
import com.fitjourney.fitjourney.service.WorkoutProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final WorkoutProgramService workoutProgramService;
    private final EnrollmentService enrollmentService;

    @Scheduled(cron = "0 0 0 * * *")
    public void archiveInactivePrograms() {
        workoutProgramService.archiveInactivePrograms();
    }

    @Scheduled(fixedRate = 600000)
    public void updateExpiredEnrollments() {
        enrollmentService.updateExpiredEnrollments();
    }
}

