package ru.covenant.code.landing.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class AdminNotFoundException extends BusinessException {

    public AdminNotFoundException(UUID adminId) {
        super("ADMIN_NOT_FOUND", "Администратор не найден", "Администратор с ID {adminId} не найден",
                HttpStatus.NOT_FOUND, Map.of("adminId", adminId.toString())
        );
    }
}
