package com.fitjourney.nutritionsvc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutritionStatusController.class)
class NutritionStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void status_shouldReturnNutritionServiceStatus() throws Exception {
        mockMvc.perform(get("/nutrition/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("nutrition-svc"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
