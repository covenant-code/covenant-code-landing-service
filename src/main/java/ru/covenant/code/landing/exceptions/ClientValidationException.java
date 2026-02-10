package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ClientValidationException extends BusinessException {
    private final Map<String, String> validationErrors;

    public ClientValidationException(String message, Map<String, String> validationErrors) {
        super(
                "CLIENT_VALIDATION_ERROR",
                "Ошибка валидации данных клиента",
                message,
                HttpStatus.BAD_REQUEST,
                validationErrors
        );
        this.validationErrors = validationErrors;
    }

    public static ClientValidationException of(String field, String error) {
        return new ClientValidationException(
                String.format("Ошибка валидации поля '%s': %s", field, error),
                Map.of(field, error)
        );
    }
}
