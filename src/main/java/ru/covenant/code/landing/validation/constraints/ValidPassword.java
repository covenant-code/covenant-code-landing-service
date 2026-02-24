package ru.covenant.code.landing.validation.constraints;

import jakarta.validation.Constraint;
import ru.covenant.code.landing.validation.validators.PasswordValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = PasswordValidator.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Пароль должен содержать минимум 8 символов, включая цифры и буквы";

    int min() default 8;

    int max() default 100;
}
