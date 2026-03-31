package ru.covenant.code.landing.validation.constraints;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.covenant.code.landing.validation.validators.EnumValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = EnumValidator.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

    String message() default "Недопустимое значение";


    Class<? extends Enum<?>> enumClass();


    boolean nullable() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
