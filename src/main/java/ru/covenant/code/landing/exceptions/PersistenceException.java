package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class PersistenceException extends BusinessException {
    private final String entityName;
    private final String operation;

    public PersistenceException(String entityName, String operation, Throwable cause) {
        super(
                "PERSISTENCE_ERROR",
                "Ошибка сохранения данных",
                String.format("Ошибка при выполнении операции '%s' для сущности '%s'", operation, entityName),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Map.of(
                        "entityName", entityName,
                        "operation", operation,
                        "cause", cause != null ? cause.getMessage() : "Неизвестно"
                )
        );
        this.entityName = entityName;
        this.operation = operation;
    }

    public static PersistenceException save(String entityName, Throwable cause) {
        return new PersistenceException(entityName, "сохранение", cause);
    }

    public static PersistenceException update(String entityName, Throwable cause) {
        return new PersistenceException(entityName, "обновление", cause);
    }

    public static PersistenceException delete(String entityName, Throwable cause) {
        return new PersistenceException(entityName, "удаление", cause);
    }
}
