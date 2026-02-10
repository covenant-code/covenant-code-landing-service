package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import ru.covenant.code.landing.validation.constraints.ValidPhone;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$" // E.164 формат
    );

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(phone)) {
            return true; // Для обязательности используем @NotBlank
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}