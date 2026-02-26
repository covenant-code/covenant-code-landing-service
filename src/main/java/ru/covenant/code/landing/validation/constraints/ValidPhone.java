package ru.covenant.code.landing.validation.constraints;

import jakarta.validation.Constraint;
import ru.covenant.code.landing.validation.validators.PhoneValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = PhoneValidator.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Неверный формат телефона";
}
