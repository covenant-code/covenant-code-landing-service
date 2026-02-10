package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

@Getter
public class AdminNotFoundException extends BusinessException {
    private final UUID adminId;

    public AdminNotFoundException(UUID adminId) {
        super(
                "ADMIN_NOT_FOUND",
                "Администратор не найден",
                String.format("Администратор с ID %s не найден", adminId),
                HttpStatus.NOT_FOUND,
                Map.of("adminId", adminId.toString())
        );
        this.adminId = adminId;
    }
}