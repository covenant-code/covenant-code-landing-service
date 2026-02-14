package ru.covenant.code.landing.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ResponseWrapperTest {

    @Test
    void testSuccessResponseSerialization() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Успешный ответ с данными
        ResponseWrapper<String> response = ResponseWrapper.success("Hello");
        String json = mapper.writeValueAsString(response);

        // Ожидаемый JSON
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"result\":\"Hello\""));
        assertFalse(json.contains("error")); // Поле error должно отсутствовать
    }

    @Test
    void testErrorResponseSerialization() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Ответ с ошибкой
        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Ошибка валидации",
                "Имя обязательно"
        );
        ResponseWrapper<String> response = ResponseWrapper.error(error);
        String json = mapper.writeValueAsString(response);

        // Ожидаемый JSON
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("\"code\":\"VALIDATION_ERROR\""));
        assertTrue(json.contains("\"description\":\"Ошибка валидации\""));
    }

    @Test
    void testFactoryMethods() {
        // Проверка успешного ответа с данными
        ResponseWrapper<String> successWithData = ResponseWrapper.success("test");
        assertTrue(successWithData.isSuccess());
        assertEquals("test", successWithData.getResult());
        assertNull(successWithData.getError());

        // Проверка успешного ответа без данных
        ResponseWrapper<Void> successWithoutData = ResponseWrapper.success();
        assertTrue(successWithoutData.isSuccess());
        assertNull(successWithoutData.getResult());
        assertNull(successWithoutData.getError());

        // Проверка ответа с ошибкой
        ResponseWrapper<String> error = ResponseWrapper.error("NOT_FOUND", "Ресурс не найден");
        assertFalse(error.isSuccess());
        assertNotNull(error.getError());
        assertEquals("NOT_FOUND", error.getError().getCode());
    }
}