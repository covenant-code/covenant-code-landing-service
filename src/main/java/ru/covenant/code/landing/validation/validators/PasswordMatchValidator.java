package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import ru.covenant.code.landing.validation.constraints.PasswordMatch;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmField;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmField = constraintAnnotation.confirmField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object password = wrapper.getPropertyValue(passwordField);
        Object confirm = wrapper.getPropertyValue(confirmField);

        if (password == null && confirm == null) {
            return true;
        }

        return password != null && password.equals(confirm);
    }
}