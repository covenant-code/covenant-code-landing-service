package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import ru.covenant.code.landing.validation.constraints.ValidName;

import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<ValidName, String> {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\p{L} .'-]+$", Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(name)) {
            return true; // Для обязательности используем @NotBlank
        }
        return NAME_PATTERN.matcher(name).matches() && name.length() >= 2;
    }
}