package ru.covenant.code.landing.validation.validators;

import jakarta.validation.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.covenant.code.landing.testDTO.ChangePasswordDto;
import ru.covenant.code.landing.validation.validators.PasswordMatchValidator;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordMatchValidatorTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    private final PasswordMatchValidator validatorMatch =
            new PasswordMatchValidator();

    @BeforeAll
    static void globalSetUp() {

        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void globalTearDown() {
        factory.close();
    }


    @Test
    @DisplayName("Пароли одинаковые")
    void identicalPasswords() {
        ChangePasswordDto dto = new ChangePasswordDto("asd", "asd");


        Set<ConstraintViolation<ChangePasswordDto>> validate = validator.validate(dto);
//        org.assertj.core.api.Assertions.assertThat(validate).as("Список должен быть пуст")
//                .hasSize(0);
//       assertFalse(!validate.isEmpty(), "Не найдено ни одной ошибки");
        assertTrue(validate.isEmpty(), "Список должен быть пуст" + validate);
//        if (!validate.isEmpty()) {
//            Assertions.fail("Ожидали 0 ошибок, но найдены: " + validate);
//        }


    }

    @Test
    @DisplayName("Пароли одинаковые, содержат null")
    void identicalPasswordsNull() {
        ChangePasswordDto passwordDto =
                new ChangePasswordDto(null, null);
        Set<ConstraintViolation<ChangePasswordDto>> validate = validator.validate(passwordDto);
//        org.assertj.core.api.Assertions.assertThat(validate).as("Список должен быть пуст")
//                .hasSize(0);
        if (!validate.isEmpty()) {
            Assertions.fail("Ожидали 0 ошибок, но найдены: " + validate);
        }

    }


    @Test
    @DisplayName("Ошибка валидации при вводе различных паролей")
    void differentPassword() {
        ChangePasswordDto passwordDto =
                new ChangePasswordDto("asd", "dsa");
        Set<ConstraintViolation<ChangePasswordDto>> validate = validator.validate(passwordDto);
        assertEquals(1, validate.size(),
                "Должна быть ровно 1 ошибка, а найдено: " + validate.size());

        ConstraintViolation<ChangePasswordDto> v = validate.iterator().next();

        assertEquals("Пароли не совпадают", v.getMessage(),
                "Сообщение об ошибке неверное");

        assertEquals("", v.getPropertyPath().toString(),
                "Путь должен быть пустым");
    }
}