package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientValidationException extends BusinessException {

    Map<String, String> validationErrors;

    public ClientValidationException(String message, Map<String, String> validationErrors) {
        super("CLIENT_VALIDATION_ERROR", "Ошибка валидации данных клиента", message,
                HttpStatus.BAD_REQUEST, validationErrors);
        this.validationErrors = validationErrors;
    }

    public static ClientValidationException of(String field, String error) {
        String message = String.format("Ошибка валидации поля '{%s}': {%s}", field, error);
        return new ClientValidationException(message, Collections.singletonMap(field, error));
    }
}
