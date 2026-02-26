package ru.covenant.code.landing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.covenant.code.landing.validation.constraints.ValidName;

import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<ValidName, String> {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\p{L} .'-]+$", Pattern.UNICODE_CHARACTER_CLASS
    );


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.trim().isEmpty()){
            return true;
        }

        if(value.trim().length() < 2){
            return false;
        }

        return NAME_PATTERN.matcher(value).matches();
    }
}
