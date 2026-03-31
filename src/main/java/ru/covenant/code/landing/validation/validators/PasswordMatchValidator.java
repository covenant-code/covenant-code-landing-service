package ru.covenant.code.landing.validation.validators;

import ru.covenant.code.landing.validation.constraints.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.Objects;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    private String passwordField;
    private String confirmField;


    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmField = constraintAnnotation.confirmField();
    }

    @Override
    public boolean isValid(Object value,
                           ConstraintValidatorContext constraintValidatorContext) {
        if(value==null){
            return true;
        }

//        BeanWrapperImpl beanWrapper = (BeanWrapperImpl)PropertyAccessorFactory.forBeanPropertyAccess(value);
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);
        Object passwordValue = beanWrapper.getPropertyValue(passwordField);
        Object confirmPassValue = beanWrapper.getPropertyValue(confirmField);

        if(passwordValue ==null || confirmPassValue == null){
            return true;
        } else return Objects.equals(passwordValue.toString(),
                confirmPassValue.toString());

    }
}
