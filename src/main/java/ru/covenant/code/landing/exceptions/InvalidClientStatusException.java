package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static java.lang.String.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidClientStatusException extends BusinessException {

    String currentStatus;
    String attemptedStatus;

    public InvalidClientStatusException(String currentStatus, String attemptedStatus) {
        super("INVALID_CLIENT_STATUS", "Некорректное изменение статуса",
                format("Невозможно изменить статус с '{%s}' на '{%s}'", currentStatus, attemptedStatus),
                HttpStatus.BAD_REQUEST, Map.of(currentStatus, attemptedStatus));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
}
