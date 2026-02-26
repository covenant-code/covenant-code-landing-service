package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.covenant.code.landing.validation.constraints.ValidPassword;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[a-zA-Z].*");
    private int min;
    private int max;

    @Override
    public void initialize(ValidPassword annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        if (value.trim().length() < min && value.trim().length() > max) {
            return false;
        }


        return HAS_DIGIT.matcher(value).matches() && HAS_LETTER.matcher(value).matches();
    }


}
