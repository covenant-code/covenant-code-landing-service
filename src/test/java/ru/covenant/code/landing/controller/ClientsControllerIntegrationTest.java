package ru.covenant.code.landing.controller;


import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import ru.covenant.code.landing.dto.client.response.CourseInfoDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc   // поднимает весь контекст приложения
public class ClientsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /* Доступность эндпоинта (должен вернуть 200) */
    @Test
    @DisplayName("GET /api/v1/clients/courses – endpoint доступен")
    void endpointIsAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses"))
                .andExpect(status().isOk());
    }

    /* Проверка Content‑Type = application/json */
    @Test
    @DisplayName("GET /api/v1/clients/courses – Content‑Type = application/json")
    void contentTypeIsJson() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /* Проверка структуры JSON‑ответа */
    @Test
    @DisplayName("GET /api/v1/clients/courses – JSON структура валидна")
    void jsonStructureIsCorrect() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // корневой объект – ResponseWrapper
                .andExpect(jsonPath("$.result").isArray())
                // каждый элемент массива – объект CourseInfoDto с обязательными полями
                .andExpect(jsonPath("$.result[*].code", everyItem(isA(String.class))))
                .andExpect(jsonPath("$.result[*].name", everyItem(isA(String.class))))
                .andExpect(jsonPath("$.result[*].description", everyItem(isA(String.class))))
                .andExpect(jsonPath("$.result[*].duration", everyItem(isA(String.class))))
                .andExpect(jsonPath("$.result[*].price", everyItem(isA(String.class))))
                // проверяем, что массив не пустой (по крайней мере один курс)
                .andExpect(jsonPath("$.result", hasSize(greaterThan(0))));
    }

    /* Доступ без аутентификации (Security‑test) */
    @Test
    @DisplayName("GET /api/v1/clients/courses – доступ без авторизации")
    void endpointAccessibleWithoutAuthentication() throws Exception {
        // без указания заголовков Authorization
        mockMvc.perform(get("/api/v1/clients/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}

