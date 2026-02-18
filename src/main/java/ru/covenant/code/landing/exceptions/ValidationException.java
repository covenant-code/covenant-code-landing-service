package ru.covenant.code.landing.exceptions;

import org.springframework.http.HttpStatus;

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
