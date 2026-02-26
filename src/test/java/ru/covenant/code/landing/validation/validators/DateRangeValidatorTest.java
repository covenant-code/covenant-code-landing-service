package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.testDTO.DateRangeDto;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class DateRangeValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("startDate = 2024-01-01, endDate = 2024-01-02 → успех")
    void startDateBeforeEndDate_success() {
        DateRangeDto dto = new DateRangeDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(LocalDate.of(2024, 1, 2));

        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Ожидали 0 ошибок, но найдены: " + violations);
    }

    @Test
    @DisplayName("startDate = 2024-01-02, endDate = 2024-01-01 → ошибка")
    void startDateAfterEndDate_failure() {
        DateRangeDto dto = new DateRangeDto();
        dto.setStartDate(LocalDate.of(2024, 1, 2));
        dto.setEndDate(LocalDate.of(2024, 1, 1));

        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(),
                "Ожидали 1 ошибку, но не найдено ни одной");

        ConstraintViolation<DateRangeDto> violation = violations.iterator().next();
        assertEquals("Конечная дата должна быть после начальной", violation.getMessage(),
                "Сообщение об ошибке не соответствует ожидаемому");
    }

    @Test
    @DisplayName("startDate = null, endDate = 2024-01-01 → успех")
    void startDateNull_success() {
        DateRangeDto dto = new DateRangeDto();
        dto.setStartDate(null);
        dto.setEndDate(LocalDate.of(2024, 1, 1));

        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Ожидали 0 ошибок при null startDate, но найдены: " + violations);
    }

    @Test
    @DisplayName("startDate = 2024-01-01, endDate = null → успех")
    void endDateNull_success() {
        DateRangeDto dto = new DateRangeDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(null);

        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Ожидали 0 ошибок при null endDate, но найдены: " + violations);
    }

    @Test
    @DisplayName("startDate = null, endDate = null → успех")
    void bothNull_success() {
        DateRangeDto dto = new DateRangeDto();
        dto.setStartDate(null);
        dto.setEndDate(null);

        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Ожидали 0 ошибок при обоих null, но найдены: " + violations);
    }
}



