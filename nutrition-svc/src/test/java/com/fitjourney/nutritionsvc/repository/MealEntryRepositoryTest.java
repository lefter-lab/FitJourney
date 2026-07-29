package com.fitjourney.nutritionsvc.repository;

import com.fitjourney.nutritionsvc.entity.DayOfWeek;
import com.fitjourney.nutritionsvc.entity.MealEntry;
import com.fitjourney.nutritionsvc.entity.NutritionPlan;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class MealEntryRepositoryTest {

    @Autowired
    private MealEntryRepository mealEntryRepository;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByNutritionPlanId_shouldReturnMealsForPlan() {
        NutritionPlan plan = nutritionPlanRepository.saveAndFlush(nutritionPlan(UUID.randomUUID(), "Balanced Plan"));
        MealEntry breakfast = mealEntryRepository.save(mealEntry(plan, "Breakfast", 500, 30, 45, 12, DayOfWeek.MONDAY));
        MealEntry dinner = mealEntryRepository.save(mealEntry(plan, "Dinner", 700, 40, 60, 20, DayOfWeek.FRIDAY));
        mealEntryRepository.flush();

        List<MealEntry> result = mealEntryRepository.findAllByNutritionPlanId(plan.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MealEntry::getMealName).containsExactlyInAnyOrder("Breakfast", "Dinner");
        assertThat(result).extracting(MealEntry::getCalories).containsExactlyInAnyOrder(500, 700);
        assertThat(result).extracting(MealEntry::getProtein).containsExactlyInAnyOrder(30, 40);
        assertThat(result).extracting(MealEntry::getCarbs).containsExactlyInAnyOrder(45, 60);
        assertThat(result).extracting(MealEntry::getFats).containsExactlyInAnyOrder(12, 20);
        assertThat(result).extracting(MealEntry::getDayOfWeek).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        assertThat(result).extracting(entry -> entry.getNutritionPlan().getId()).containsOnly(plan.getId());
        assertThat(result).extracting(MealEntry::getId).containsExactlyInAnyOrder(breakfast.getId(), dinner.getId());
    }

    @Test
    void findAllByNutritionPlanId_shouldIsolateMealsBetweenPlans() {
        NutritionPlan firstPlan = nutritionPlanRepository.saveAndFlush(nutritionPlan(UUID.randomUUID(), "First Plan"));
        NutritionPlan secondPlan = nutritionPlanRepository.saveAndFlush(nutritionPlan(UUID.randomUUID(), "Second Plan"));
        mealEntryRepository.save(mealEntry(firstPlan, "Breakfast", 500, 30, 45, 12, DayOfWeek.MONDAY));
        mealEntryRepository.save(mealEntry(secondPlan, "Dinner", 700, 40, 60, 20, DayOfWeek.TUESDAY));
        mealEntryRepository.flush();

        List<MealEntry> result = mealEntryRepository.findAllByNutritionPlanId(firstPlan.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMealName()).isEqualTo("Breakfast");
        assertThat(result.get(0).getNutritionPlan().getId()).isEqualTo(firstPlan.getId());
    }

    @Test
    void findAllByNutritionPlanId_shouldReturnEmptyListWhenPlanHasNoMeals() {
        NutritionPlan plan = nutritionPlanRepository.saveAndFlush(nutritionPlan(UUID.randomUUID(), "Balanced Plan"));

        List<MealEntry> result = mealEntryRepository.findAllByNutritionPlanId(plan.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void saveAndFlush_shouldPersistDayOfWeekEnumAsStringValue() {
        NutritionPlan plan = nutritionPlanRepository.saveAndFlush(nutritionPlan(UUID.randomUUID(), "Balanced Plan"));
        MealEntry savedMeal = mealEntryRepository.saveAndFlush(
                mealEntry(plan, "Saturday Breakfast", 550, 35, 50, 15, DayOfWeek.SATURDAY)
        );
        entityManager.clear();

        MealEntry result = mealEntryRepository.findById(savedMeal.getId()).orElseThrow();

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
    }

    @Test
    void saveAndFlush_shouldRejectMealEntryWithoutNutritionPlan() {
        MealEntry entry = mealEntry(null, "Breakfast", 500, 30, 45, 12, DayOfWeek.MONDAY);

        assertThatThrownBy(() -> mealEntryRepository.saveAndFlush(entry))
                .isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
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

    private static MealEntry mealEntry(NutritionPlan plan,
                                       String mealName,
                                       int calories,
                                       int protein,
                                       int carbs,
                                       int fats,
                                       DayOfWeek dayOfWeek) {
        MealEntry entry = new MealEntry();
        entry.setNutritionPlan(plan);
        entry.setMealName(mealName);
        entry.setCalories(calories);
        entry.setProtein(protein);
        entry.setCarbs(carbs);
        entry.setFats(fats);
        entry.setDayOfWeek(dayOfWeek);
        return entry;
    }
}
