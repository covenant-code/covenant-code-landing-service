package ru.covenant.code.landing.controller;


import org.junit.jupiter.api.Disabled;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.CourseInfoDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc // поднимает весь контекст приложения
@ActiveProfiles("test")
@Transactional
public class ClientsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    // Успешное создание заявки (HTTP 201)
    @Test
    public void testCreateApplication_Success_Returns201() throws Exception {
        String validRequest = """
            {
                "name": "Ivan Petrov",
                "email": "ivan@example.com",
                "phone": "+79001234567",
                "message": "Интересуют курсы",
                "courseType": "FULLSTACK",
                "source": "Лендинг",
                "courseTypeEnum": "BACKEND"
            }
            """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(jsonPath("$.result[*].name").value("Ivan Petrov"))
                .andExpect(jsonPath("$.result[*].email").value("ivan@example.com"))
                .andExpect(jsonPath("$.result[*].phone").value("+79001234567"))
                .andExpect(jsonPath("$.result[*].courseType").value("FULLSTACK"));
    }
    @Test
    void testSuccessfulPostRequest() throws Exception {
        ClientsRqDto request = ClientsRqDto.builder()
                .name("Ян Иванов")
                .email("test1@example.com")
                .phone("+79161234569")
                .message("Хочу записаться на курс")
                .courseType("BACKEND")
                .build();

        // Mock сервиса (если нужно)
        // when(clientsService.createClient(any())).thenReturn(...);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Проверка вложенного объекта result
                .andExpect(jsonPath("$.result[*].name").value("Ян Иванов"))
                .andExpect(jsonPath("$.result[*].email").value("test1@example.com"))
                .andExpect(jsonPath("$.result[*].courseType").value("BACKEND"))
                .andExpect(jsonPath("$.result[*].status").value("SUCCESS"))
                // Для createdAt лучше проверять наличие поля без точного значения
                .andExpect(jsonPath("$.result[*].createdAt").exists());
    }
@Disabled
    @Test
    void testValidationErrors() throws Exception {
        // Тестируем некорректный email
        ClientsRqDto invalidEmailRequest = ClientsRqDto.builder()
                .name("Ян Иванов")
                .email("invalid-email")
                .phone("+79161234569")
                .message("Хочу записаться на курс")
                .courseType("BACKEND")
                .source("Лендинг")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.description").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.error.details.email").exists());

        // Тестируем некорректный телефон
        ClientsRqDto invalidPhoneRequest = ClientsRqDto.builder()
                .name("Ян Иванов")
                .email("test1@example.com")
                .phone("89161234569")
                .message("Хочу записаться на курс")
                .courseType("BACKEND")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPhoneRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.phone").exists());

        // Тестируем некорректный тип курса
        ClientsRqDto invalidCourseRequest = ClientsRqDto.builder()
                .name("Ян Иванов")
                .email("test1@example.com")
                .phone("+79161234569")
                .message("Хочу записаться на курс")
                .courseType("BACKEND")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourseRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.courseType").exists());
    }
@Disabled
    @Test
    void testDuplicateEmail() throws Exception {
        // Сначала создаем клиента
        ClientsRqDto firstRequest = ClientsRqDto.builder()
                .name("Ян Иванов")
                .email("duplicate@example.com")
                .phone("+79161234569")
                .message("Хочу записаться на курс")
                .courseType("BACKEND")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Пробуем создать клиента с тем же email
        ClientsRqDto duplicateRequest = ClientsRqDto.builder()
                .name("Петр Петров")
                .email("duplicate@example.com")
                .phone("+79161234568")
                .message("Хочу записаться на курс")
                .courseType("FRONTEND")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("CLIENT_DUPLICATE"))
                .andExpect(jsonPath("$.error.message").value("Заявка с email 'duplicate@example.com' или телефоном '+79161234568' уже существует"))
                .andReturn();
    }

    @Test
    void testSwaggerDocumentation() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // Проверка наличия endpoints в Swagger
        // (В реальном приложении это может требовать отдельного теста для Swagger UI)
        // Здесь мы просто проверяем, что endpoint доступен и возвращает ожидаемый статус
    }

}

