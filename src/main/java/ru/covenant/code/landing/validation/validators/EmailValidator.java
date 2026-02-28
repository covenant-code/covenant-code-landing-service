package ru.covenant.code.landing.validation.validators;

import ru.covenant.code.landing.validation.constraints.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


import java.util.regex.Pattern;


public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );


    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if(email == null || email.isEmpty()){
        return true;
        }
        else return EMAIL_PATTERN.matcher(email).matches();
    }
}
