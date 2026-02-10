package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import ru.covenant.code.landing.validation.constraints.ValidDateRange;

import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startDateField;
    private String endDateField;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        LocalDate startDate = (LocalDate) wrapper.getPropertyValue(startDateField);
        LocalDate endDate = (LocalDate) wrapper.getPropertyValue(endDateField);

        if (startDate == null || endDate == null) {
            return true; // Пусть другие валидаторы проверяют обязательность
        }

        return !endDate.isBefore(startDate);
    }
}