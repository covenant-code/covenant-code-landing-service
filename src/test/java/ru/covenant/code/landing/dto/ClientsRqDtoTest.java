package ru.covenant.code.landing.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClientsRqDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ClientsRqDto validDto() {
        return ClientsRqDto.builder()
                .name("Иван Петров")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Хочу узнать подробнее")
                .courseType("BACKEND")
                .source("Лендинг")
                .build();
    }

    @Test
    void validDto_shouldPassValidation() {
        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(validDto());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankName_shouldFailValidation() {
        ClientsRqDto dto = validDto();
        dto.setName("");

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void invalidEmail_shouldFailValidation() {
        ClientsRqDto dto = validDto();
        dto.setEmail("неправильный-email");

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void invalidPhone_shouldFailValidation() {
        ClientsRqDto dto = validDto();
        dto.setPhone("123abc");

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phone"));
    }

    @Test
    void invalidCourseType_shouldFailValidation() {
        ClientsRqDto dto = validDto();
        dto.setCourseType("INVALID_COURSE");

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("courseType"));
    }

    @Test
    void messageTooLong_shouldFailValidation() {
        ClientsRqDto dto = validDto();
        dto.setMessage("а".repeat(1001));

        Set<ConstraintViolation<ClientsRqDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("message"));
    }
}
