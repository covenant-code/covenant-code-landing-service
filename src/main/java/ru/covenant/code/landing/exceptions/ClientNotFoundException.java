package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static java.lang.String.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientNotFoundException extends BusinessException {

    UUID clientId;

    public ClientNotFoundException(UUID clientId) {
        super("CLIENT_NOT_FOUND", "Заявка не найдена", format("Заявка с ID {%s} не найдена", clientId.toString()),
                HttpStatus.NOT_FOUND, Map.of("clientId", clientId.toString()));
        this.clientId = clientId;
    }
}
