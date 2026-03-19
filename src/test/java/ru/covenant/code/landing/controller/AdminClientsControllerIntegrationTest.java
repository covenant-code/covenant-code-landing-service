package ru.covenant.code.landing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.repository.ClientsRepository;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminClientsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientsRepository clientsRepository;

    @BeforeEach
    void setUp() {
        clientsRepository.deleteAll();
        createTestClients();
    }

    private void createTestClients() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        List<Clients> testClients = Arrays.asList(
                Clients.builder()
                        .name("Анна Смирнова")
                        .email("anna@example.com")
                        .phone("+79161239876")
                        .message("Записалась на курс")
                        .courseType(CourseType.BACKEND)
                        .status(Status.DONE)
                        .priority(Priority.MEDIUM)
                        .source("Лендинг")
                        .processedBy("admin@covenantcode.ru")
                        .processedAt(now.minusDays(7).plusHours(1))
                        .createdAt(now.minusDays(7))
                        .updatedAt(now.minusDays(7).plusHours(1))
                        .build(),

                Clients.builder()
                        .name("Сергей Сергеев")
                        .email("sergey@example.com")
                        .phone("+79169876543")
                        .message("Frontend разработка")
                        .courseType(CourseType.FRONTEND)
                        .status(Status.PROCESSED)
                        .priority(Priority.LOW)
                        .source("Лендинг")
                        .processedBy("admin@covenantcode.ru")
                        .processedAt(now.minusDays(3).plusHours(1))
                        .createdAt(now.minusDays(3))
                        .updatedAt(now.minusDays(3).plusHours(1))
                        .build(),

                Clients.builder()
                        .name("Иван Петров")
                        .email("ivan@example.com")
                        .phone("+79161234567")
                        .message("Хочу на Fullstack")
                        .courseType(CourseType.FULLSTACK)
                        .status(Status.NEW)
                        .priority(Priority.HIGH)
                        .source("Лендинг")
                        .createdAt(now.minusDays(1))
                        .updatedAt(now.minusDays(1))
                        .build(),

                Clients.builder()
                        .name("Петр Иванов")
                        .email("petr@example.com")
                        .phone("+79167654321")
                        .message("Интересует Backend")
                        .courseType(CourseType.BACKEND)
                        .status(Status.NEW)
                        .priority(Priority.MEDIUM)
                        .source("Лендинг")
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        clientsRepository.saveAll(testClients);
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – успешное получение списка клиентов с фильтром по статусу")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithStatusFilterSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[*].status", everyItem(is("NEW"))))
                .andExpect(jsonPath("$.result[*].statusLabel", everyItem(is("Новые"))))
                .andExpect(jsonPath("$.result[*].name", containsInAnyOrder("Иван Петров", "Петр Иванов")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – проверка наличия всех полей в ответе")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void verifyResponseFields() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").exists())
                .andExpect(jsonPath("$.result[0].name").exists())
                .andExpect(jsonPath("$.result[0].email").exists())
                .andExpect(jsonPath("$.result[0].phone").exists())
                .andExpect(jsonPath("$.result[0].message").exists())
                .andExpect(jsonPath("$.result[0].courseType").exists())
                .andExpect(jsonPath("$.result[0].status").exists())
                .andExpect(jsonPath("$.result[0].priority").exists())
                .andExpect(jsonPath("$.result[0].statusLabel").exists())
                .andExpect(jsonPath("$.result[0].priorityLabel").exists())
                .andExpect(jsonPath("$.result[0].source").exists())
                .andExpect(jsonPath("$.result[0].createdAt").exists())
                .andExpect(jsonPath("$.result[0].updatedAt").exists())
                .andExpect(jsonPath("$.result[0].formattedCreatedAt").exists())
                .andExpect(jsonPath("$.result[0].formattedUpdatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – ошибка при некорректном статусе")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Проверьте правильность заполнения полей"))
                .andExpect(jsonPath("$.error.details.statuses").exists());
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – работа с разными регистрами статуса")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithDifferentCaseStatus() throws Exception {
        // В вашем приложении регистр имеет значение, поэтому все запросы с неправильным регистром должны возвращать 400
        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "new") // нижний регистр
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "New") // смешанный регистр
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Только верхний регистр должен работать
        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – фильтрация по нескольким статусам")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithMultipleStatuses() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW,PROCESSED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(3)))
                .andExpect(jsonPath("$.result[*].status", containsInAnyOrder("NEW", "NEW", "PROCESSED")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – доступ без аутентификации")
    void endpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – доступ с недостаточными правами")
    @WithMockUser(username = "user@example.com", roles = "USER")
    void endpointWithInsufficientPermissions() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}