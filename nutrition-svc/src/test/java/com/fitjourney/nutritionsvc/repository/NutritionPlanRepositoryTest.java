package com.fitjourney.nutritionsvc.repository;

import com.fitjourney.nutritionsvc.entity.NutritionPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class NutritionPlanRepositoryTest {

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Test
    void findByProgramId_shouldReturnSavedPlan() {
        UUID programId = UUID.randomUUID();
        NutritionPlan savedPlan = nutritionPlanRepository.saveAndFlush(nutritionPlan(programId, "Balanced Plan"));

        Optional<NutritionPlan> result = nutritionPlanRepository.findByProgramId(programId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedPlan.getId());
        assertThat(result.get().getProgramId()).isEqualTo(programId);
        assertThat(result.get().getName()).isEqualTo("Balanced Plan");
        assertThat(result.get().getDescription()).isEqualTo("Balanced nutrition plan");
        assertThat(result.get().getDailyCalories()).isEqualTo(2200);
        assertThat(result.get().getCreatedAt()).isEqualTo(savedPlan.getCreatedAt());
    }

    @Test
    void findByProgramId_shouldReturnEmptyWhenProgramIdDoesNotExist() {
        Optional<NutritionPlan> result = nutritionPlanRepository.findByProgramId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByProgramId_shouldReturnTrueForSavedPlanAndFalseForMissingPlan() {
        UUID programId = UUID.randomUUID();
        nutritionPlanRepository.saveAndFlush(nutritionPlan(programId, "Balanced Plan"));

        assertThat(nutritionPlanRepository.existsByProgramId(programId)).isTrue();
        assertThat(nutritionPlanRepository.existsByProgramId(UUID.randomUUID())).isFalse();
    }

    @Test
    void saveAndFlush_shouldEnforceUniqueProgramIdConstraint() {
        UUID programId = UUID.randomUUID();
        nutritionPlanRepository.saveAndFlush(nutritionPlan(programId, "Balanced Plan"));

        assertThatThrownBy(() -> nutritionPlanRepository.saveAndFlush(nutritionPlan(programId, "Second Plan")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static NutritionPlan nutritionPlan(UUID programId, String name) {
        NutritionPlan plan = new NutritionPlan();
        plan.setProgramId(programId);
        plan.setName(name);
        plan.setDescription("Balanced nutrition plan");
        plan.setDailyCalories(2200);
        plan.setCreatedAt(LocalDateTime.now());
        return plan;
    }
}
