package ru.covenant.code.landing.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.covenant.code.landing.validation.validators.PasswordValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Пароль должен содержать минимум 8 символов, включая цифры и буквы";
    int min() default 8;
    int max() default 100;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}