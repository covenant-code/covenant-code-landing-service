package ru.covenant.code.landing;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.validation.Validator;
import ru.covenant.code.landing.dto.TestDto;


import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ValidationConfigTest {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private Validator validator;

    @Test
    void messageSourceBeanExists() {
        assertNotNull(messageSource);
    }

    @Test
    void validatorBeanExists() {
        assertNotNull(validator);
    }

    @Test
    void testMessageSource() {
        String message = messageSource.getMessage(
                "NotBlank",
                null,
                Locale.getDefault()
        );
        assertEquals("Поле не может быть пустым", message);
    }
}