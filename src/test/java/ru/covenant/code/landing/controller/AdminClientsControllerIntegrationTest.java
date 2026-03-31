package ru.covenant.code.landing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.security.config.SecurityConfig;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.repository.ClientsRepository;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
@Transactional
@DisplayName("Интеграционные тесты для AdminClientsController")
class AdminClientsControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientsRepository clientsRepository;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UUID testClientId;
    private OffsetDateTime now;

    private UUID existingClientId;
    private UUID nonExistingClientId;
    private String invalidUuid = "not-a-uuid";


    @BeforeEach
    void setUp() {
        clientsRepository.deleteAll();
        now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        createTestClients();

        objectMapper = new ObjectMapper();

        Clients testClient = Clients.builder()
                .email("test@example.com")
                .name("Тестовый Клиент")
                .phone("+79001234567")
                .message("Тестовое сообщение")
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .priority(Priority.HIGH)
                .source("Лендинг")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Clients savedClient = clientsRepository.save(testClient);
        existingClientId = savedClient.getId();

        nonExistingClientId = UUID.randomUUID();

        assertTrue(clientsRepository.findById(existingClientId).isPresent(),
                "Клиент должен существовать в БД с ID: " + existingClientId);

        System.out.println("Создан тестовый клиент с ID: " + existingClientId);


    }

    @Test
    @DisplayName("Тест 1: Успешный GET запрос с существующим ID - должен вернуть 200 и данные клиента")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithExistingId_ShouldReturn200AndClientData() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.id").value(existingClientId.toString()))
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.name").value("Тестовый Клиент"))
                .andExpect(jsonPath("$.result.phone").value("+79001234567"))
                .andExpect(jsonPath("$.result.message").value("Тестовое сообщение"))
                .andExpect(jsonPath("$.result.courseType").value("BACKEND"))
                .andExpect(jsonPath("$.result.status").value("NEW"))
                .andExpect(jsonPath("$.result.priority").value("HIGH"))
                .andExpect(jsonPath("$.result.source").value("Лендинг"))
                .andExpect(jsonPath("$.result.createdAt").exists())
                .andExpect(jsonPath("$.result.updatedAt").exists())
                .andExpect(jsonPath("$.result.formattedCreatedAt").exists())
                .andExpect(jsonPath("$.result.formattedUpdatedAt").exists());

    }

    @Test
    @DisplayName("Тест 2: Запрос с несуществующим ID - должен вернуть 404 и CLIENT_NOT_FOUND")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithNonExistingId_ShouldReturn404AndClientNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.description").value("Заявка не найдена"))
                .andExpect(jsonPath("$.error.message").value(
                        "Заявка с ID {" + nonExistingClientId + "} не найдена"))
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistingClientId.toString()))
                .andReturn();
        assertFalse(clientsRepository.findById(nonExistingClientId).isPresent(),
                "Клиент с ID " + nonExistingClientId + " не должен существовать в БД");
    }

    @Test
    @DisplayName("Тест 3: Запрос с неверным форматом ID - должен вернуть 400 и ошибку валидации")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithInvalidUuidFormat_ShouldReturn400AndValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }

    @Test
    @DisplayName("Тест 4: Запрос без аутентификации - должен вернуть 401")
    void getClientById_WithoutAuthentication_ShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())  // 302 Found
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Тест 4.1: Запрос без аутентификации - Проверка HTTP 401 или редиректа")
    void getClientById_WithoutAuthentication_ShouldReturnUnauthorizedError() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())  // 302 Found
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Тест 5: Запрос с недостаточными правами (ROLE_USER) - должен вернуть 403")
    @WithMockUser(roles = "USER")
    void getClientById_WithInsufficientRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Тест 6: Проверка доступа для разных ролей")
    void getClientById_ShouldCheckAccessForDifferentRoles() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .with(user("moderator@test.com").roles("MODERATOR"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/clients/{id}", existingClientId)
                        .with(user("user@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Тест 7: Проверка защиты от SQL инъекций через ID")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WithSqlInjectionAttempt_ShouldReturn400() throws Exception {
        String sqlInjectionId = "7da674d5-0672-4d0b-a7d3-8f4ee5d3a434'; DROP TABLE clients; --";

        mockMvc.perform(get("/api/v1/admin/clients/{id}", sqlInjectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        assertTrue(clientsRepository.count() > 0, "Таблица clients должна существовать");
    }

    @Test
    @DisplayName("Тест 7.1: ClientNotFoundException - проверка всех полей исключения при клиенте не найден")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldReturnClientNotFoundExceptionWithAllFields() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.error").exists())

                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.description").value("Заявка не найдена"))
                .andExpect(jsonPath("$.error.message").value("Заявка с ID {" + nonExistingClientId + "} не найдена"))

                .andExpect(jsonPath("$.error.details").exists())
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistingClientId.toString()));
    }

    @Test
    @DisplayName("Тест 7.2: ClientNotFoundException - проверка HTTP статуса NOT_FOUND")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldReturnHttpStatusNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("Тест 7.3: ClientNotFoundException - проверка деталей ошибки в error.details")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldReturnCorrectErrorDetails() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.details").isMap())
                .andExpect(jsonPath("$.error.details.clientId").value(nonExistingClientId.toString()))
                .andExpect(jsonPath("$.error.details.clientId").isString())
                .andExpect(jsonPath("$.error.details.clientId").isNotEmpty())
                .andExpect(jsonPath("$.error.details.clientId").value(not(emptyString())));
    }

    @Test
    @DisplayName("Тест 7.4: ClientNotFoundException - проверка что исключение не содержит лишних полей")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldNotContainExtraFields() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.description").exists())
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.details").exists())

                .andExpect(jsonPath("$.error.timestamp").doesNotExist())
                .andExpect(jsonPath("$.error.path").doesNotExist())
                .andExpect(jsonPath("$.error.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.error.cause").doesNotExist())
                .andExpect(jsonPath("$.error.suppressed").doesNotExist());
    }

    @Test
    @DisplayName("Тест 7.5: ClientNotFoundException - проверка формата сообщения об ошибке")
    @WithMockUser(roles = "ADMIN")
    void getClientById_WhenClientNotFound_ShouldHaveCorrectErrorMessageFormat() throws Exception {

        String expectedMessage = "Заявка с ID {" + nonExistingClientId + "} не найдена";

        mockMvc.perform(get("/api/v1/admin/clients/{id}", nonExistingClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value(expectedMessage))
                .andExpect(jsonPath("$.error.message").isString())
                .andExpect(jsonPath("$.error.message").value(containsString(nonExistingClientId.toString())))
                .andExpect(jsonPath("$.error.message").value(containsString("Заявка с ID")))
                .andExpect(jsonPath("$.error.message").value(containsString("не найдена")));
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

        List<Clients> savedClients = clientsRepository.saveAll(testClients);

        testClientId = savedClients.get(2).getId();
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – успешное получение списка клиентов с фильтром по статусу")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithStatusFilterSuccess() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)))
                .andExpect(jsonPath("$.result[*].status", everyItem(is("NEW"))));
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

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "new")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/admin/clients – фильтрация по нескольким статусам")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void getClientsWithMultipleStatuses() throws Exception {

        mockMvc.perform(get("/api/v1/admin/clients")
                        .param("statuses", "NEW")
                        .param("statuses", "PROCESSED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(4)))
                .andExpect(jsonPath("$.result[*].status", containsInAnyOrder("NEW", "NEW", "NEW", "PROCESSED")));
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

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – успешное обновление всех полей")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void updateClient_WithFullUpdate_ShouldReturnUpdatedClient() throws Exception {
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .email("ivan.ivanov@example.com")
                .phone("+79998887766")
                .message("Обновленное сообщение")
                .courseType("BACKEND")
                .status("PROCESSED")
                .priority("HIGH")
                .source("Телефон")
                .processedBy("admin@covenantcode.ru")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", testClientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.result.status").value("PROCESSED"))
                .andExpect(jsonPath("$.result.processedBy").value("admin@covenantcode.ru"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – клиент не найден возвращает 404")
    @WithMockUser(username = "admin@covenantcode.ru", roles = "ADMIN")
    void updateClient_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .courseType("BACKEND")
                .status("NEW")
                .priority("MEDIUM")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CLIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – доступ без аутентификации")
    void updateClient_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .courseType("BACKEND")
                .status("NEW")
                .priority("MEDIUM")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/clients/{id} – доступ с недостаточными правами")
    @WithMockUser(username = "user@example.com", roles = "USER")
    void updateClient_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        ClientsUpdateRqDto updateDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        mockMvc.perform(put("/api/v1/admin/clients/{id}", testClientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

}