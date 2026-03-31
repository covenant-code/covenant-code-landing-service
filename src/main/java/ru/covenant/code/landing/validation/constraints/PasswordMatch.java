package ru.covenant.code.landing.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.covenant.code.landing.validation.validators.PasswordMatchValidator;


import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {

    String message() default "Пароли не совпадают";

    String passwordField() default "newPassword";

    String confirmField() default "confirmPassword";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
