package com.fitjourney.fitjourney.scheduler;

import com.fitjourney.fitjourney.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final EnrollmentService enrollmentService;

    @Scheduled(cron = "0 0 0 * * *")
    public void expireOverdueEnrollments() {
        enrollmentService.expireOverdueEnrollments();
    }

    @Scheduled(fixedRate = 600000)
    public void updateCompletedEnrollments() {
        enrollmentService.updateCompletedEnrollments();
    }
}

