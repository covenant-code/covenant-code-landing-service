package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.covenant.code.landing.validation.constraints.ValidPassword;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int min;
    private int max;
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[a-zA-Z].*");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return true; // Для обязательности используем @NotBlank
        }

        if (password.length() < min || password.length() > max) {
            return false;
        }

        // Проверяем, что есть хотя бы одна цифра и одна буква
        return HAS_DIGIT.matcher(password).matches() &&
                HAS_LETTER.matcher(password).matches();
    }
}