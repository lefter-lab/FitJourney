// =====================================================================
// ТОЗИ ФАЙЛ СЕ ДОБАВЯ В MAIN APP (FitJourney), НЕ В nutrition-svc!
// Папка: src/main/java/com/fitjourney/fitjourney/client/
// =====================================================================

package com.fitjourney.fitjourney.client;

import com.fitjourney.fitjourney.client.dto.MealEntryDto;
import com.fitjourney.fitjourney.client.dto.MealEntryResponseDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanDto;
import com.fitjourney.fitjourney.client.dto.NutritionPlanResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// TODO: добави в pom.xml на main app:
// <dependency>
//     <groupId>org.springframework.cloud</groupId>
//     <artifactId>spring-cloud-starter-openfeign</artifactId>
// </dependency>
//
// TODO: добави @EnableFeignClients в FitJourneyApplication.java
//
// TODO: добави в application.properties на main app:
// nutrition.service.url=http://localhost:8081

@FeignClient(name = "nutrition-svc", url = "${nutrition.service.url}")
public interface NutritionClient {

    @GetMapping("/nutrition/programs/{programId}")
    ResponseEntity<NutritionPlanResponseDto> getPlanByProgram(@PathVariable UUID programId);

    @PostMapping("/nutrition/plans")
    ResponseEntity<NutritionPlanResponseDto> createPlan(@RequestBody NutritionPlanDto dto);

    @PostMapping("/nutrition/plans/{id}/meals")
    ResponseEntity<MealEntryResponseDto> addMeal(@PathVariable UUID id, @RequestBody MealEntryDto dto);

    @DeleteMapping("/nutrition/plans/{id}")
    ResponseEntity<Void> deletePlan(@PathVariable UUID id);
}

// =====================================================================
// КАК СЕ ИЗПОЛЗВА В WorkoutProgramService (main app):
//
// При createProgram() → nutritionClient.createPlan(...)
// При deleteProgram() → nutritionClient.deletePlan(...)
// При показване на програма → nutritionClient.getPlanByProgram(...)
// =====================================================================
