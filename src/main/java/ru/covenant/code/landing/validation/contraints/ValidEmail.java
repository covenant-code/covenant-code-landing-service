package ru.covenant.code.landing.validation.contraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.covenant.code.landing.validation.validators.EmailValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    String message() default "Некорректный формат email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

