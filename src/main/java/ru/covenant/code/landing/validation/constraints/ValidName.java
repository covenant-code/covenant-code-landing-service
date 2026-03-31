package ru.covenant.code.landing.validation.constraints;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.covenant.code.landing.validation.validators.NameValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = NameValidator.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {

    String message() default "Имя должно содержать только буквы и пробелы";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
