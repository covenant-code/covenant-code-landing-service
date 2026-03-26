package ru.covenant.code.landing.dto.client.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.covenant.code.landing.entity.enumerated.CourseType;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClientsRqDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidDto() {
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Короткое сообщение")
                .courseType("BACKEND")
                .source("Лендинг")
                .build();

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Должен проходить валидацию с корректными данными");
    }

    @Test
    void testBlankName() {
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("") // Пустое имя
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Короткое сообщение")
                .courseType("BACKEND")
                .build();

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testInvalidEmail() {
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("Иван Иванов")
                .email("invalid-email") // Некорректный email
                .phone("+79161234567")
                .message("Короткое сообщение")
                .courseType("BACKEND")
                .build();

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+7123456789",    // Слишком короткий
            "+791612345678",  // Слишком длинный
            "89161234567",    // Без +7
            "+7916123456a"   // С буквами
    })
    void testInvalidPhone(String invalidPhone) {
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone(invalidPhone)
                .message("Короткое сообщение")
                .courseType("BACKEND")
                .build();

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void testInvalidCourseType() {
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Короткое сообщение")
                .courseType("INVALID_TYPE") // Некорректный тип курса
                .build();

        assertThrows(IllegalArgumentException.class, dto::getCourseTypeEnum);
    }

    @Test
    void testLongMessage() {
        String longMessage = "x".repeat(1001); // 1001 символ

        ClientsRqDto dto = ClientsRqDto.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message(longMessage)
                .courseType("BACKEND")
                .build();

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Сообщение длиной 1001 символ должно вызывать ошибку валидации");

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("message") &&
                                v.getMessageTemplate().equals("{clients.message.toolong}")),
                "Должно быть нарушение для поля message с указанным сообщением об ошибке");
    }

    @Test
    void testCourseTypeEnumConversion() {
        ClientsRqDto dto = ClientsRqDto.builder()
                .courseType("backend") // в нижнем регистре
                .build();

        assertEquals(CourseType.BACKEND, dto.getCourseTypeEnum());
    }
}