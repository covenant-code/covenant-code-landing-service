package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

@Getter
public class ClientNotFoundException extends BusinessException {
    private final UUID clientId;

    public ClientNotFoundException(UUID clientId) {
        super(
                "CLIENT_NOT_FOUND",
                "Заявка не найдена",
                String.format("Заявка с ID %s не найдена", clientId),
                HttpStatus.NOT_FOUND,
                Map.of("clientId", clientId.toString())
        );
        this.clientId = clientId;
    }
}
