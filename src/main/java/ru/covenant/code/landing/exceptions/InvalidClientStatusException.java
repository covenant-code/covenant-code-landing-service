package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class InvalidClientStatusException extends BusinessException {
    private final String currentStatus;
    private final String attemptedStatus;

    public InvalidClientStatusException(String currentStatus, String attemptedStatus) {
        super(
                "INVALID_CLIENT_STATUS",
                "Некорректное изменение статуса",
                String.format("Невозможно изменить статус с '%s' на '%s'", currentStatus, attemptedStatus),
                HttpStatus.BAD_REQUEST,
                Map.of(
                        "currentStatus", currentStatus,
                        "attemptedStatus", attemptedStatus
                )
        );
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
}
