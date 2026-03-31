package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import ru.covenant.code.landing.testDTO.UserDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    @DisplayName("\"test@example.com\" → успех")
    void validEmailTest() {
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");

        Set<ConstraintViolation<UserDto>> validate = validator.validate(dto);
        assertTrue(validate.isEmpty(), "Ожидали 0 ошибок, но найдено: " + validate);


    }


    @Test
    @DisplayName("невалидный email, без @")
    void inValidEmailTest() {
        UserDto dto = new UserDto();
        dto.setEmail("testexamplef.com");

        Set<ConstraintViolation<UserDto>> validate = validator.validate(dto);
        assertFalse(validate.isEmpty(),
                "Ожидали 1 ошибку, обнаружено: " + validate);

        ConstraintViolation<UserDto> value = validate.iterator().next();

        assertEquals("Некорректный формат email", value.getMessage(),
                "Сообщение об ошибке не соответствует ожидаемому");

        assertEquals("email", value.getPropertyPath().toString(),
                "Ошибка должна быть привязана к полю 'email'");

    }

    @Test
    @DisplayName("Пустая строка")
    void fieldEmailEmptyTest() {
        UserDto dto = new UserDto();
        dto.setEmail("");

        Set<ConstraintViolation<UserDto>> validate = validator.validateProperty(dto, "email");
        assertFalse(validate.isEmpty(), "Ожидали 0 ошибок, обнаружили: " + validate);
    }

    @Test
    @DisplayName("Проверка на null")
    void fieldEmailNullTest() {
        UserDto dto = new UserDto();
        dto.setEmail(null);

        Set<ConstraintViolation<UserDto>> validate = validator.validate(dto);
        assertFalse(validate.isEmpty(), "Ожидали 0 ошибок, обнаружили: "+ validate);
    }

}