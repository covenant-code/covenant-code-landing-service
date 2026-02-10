package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super(
                "VALIDATION_ERROR",
                "Ошибка валидации",
                message,
                HttpStatus.BAD_REQUEST,
                null
        );
    }
}
