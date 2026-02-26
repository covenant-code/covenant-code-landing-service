package ru.covenant.code.landing.validation.validators;

import ru.covenant.code.landing.validation.contraints.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.time.LocalDate;


public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    private String startField;
    private String endField;


    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startField = constraintAnnotation.startDateField();
        this.endField = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object == null) {
            return true;
        }

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);
        Object startPropertyObj= beanWrapper.getPropertyValue(startField);
        Object endPropertyObj = beanWrapper.getPropertyValue(endField);

        if(startPropertyObj == null || endPropertyObj == null){
            return true;
        }
        LocalDate startDate = (LocalDate) startPropertyObj;
        LocalDate endDate = (LocalDate) endPropertyObj;

        return !endDate.isBefore(startDate);

    }
}
