package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static java.lang.String.*;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminNotFoundException extends BusinessException {

    UUID adminId;

    public AdminNotFoundException(UUID adminId) {
        super("ADMIN_NOT_FOUND", "Администратор не найден", format("Администратор с ID {%s} не найден", adminId.toString()),
                HttpStatus.NOT_FOUND, Map.of("adminId", adminId.toString())
        );
        this.adminId = adminId;
    }
}
