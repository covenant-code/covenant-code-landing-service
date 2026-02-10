package ru.covenant.code.landing.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public final class ExceptionFactory {

    private ExceptionFactory() {}

    // Клиенты
    public static BusinessException clientNotFound(UUID clientId) {
        return new ClientNotFoundException(clientId);
    }

    public static BusinessException invalidClientStatus(String current, String attempted) {
        return new InvalidClientStatusException(current, attempted);
    }

    public static BusinessException clientDuplicate(String email, String phone) {
        return new ClientDuplicateException(email, phone);
    }

    // Администраторы
    public static BusinessException adminNotFound(UUID adminId) {
        return new AdminNotFoundException(adminId);
    }

    public static BusinessException adminEmailDuplicate(String email) {
        return new AdminEmailDuplicateException(email);
    }

    public static BusinessException adminPermissionDenied(String required, String current) {
        return new AdminPermissionException(required, current);
    }

    public static BusinessException adminPasswordWrong() {
        return AdminPasswordException.wrongCurrentPassword();
    }

    public static BusinessException adminPasswordsNotMatch() {
        return AdminPasswordException.passwordsNotMatch();
    }

    public static BusinessException adminPasswordSameAsOld() {
        return AdminPasswordException.sameAsOld();
    }

    // Общие
    public static BusinessException validationError(String message) {
        return new ValidationException(message);
    }

    public static BusinessException notFound(String resourceType, String resourceId) {
        return new ResourceNotFoundException(resourceType, resourceId);
    }

    public static BusinessException persistenceError(String entity, String operation, Throwable cause) {
        return PersistenceException.save(entity, cause);
    }

    // Статические методы для быстрого создания
    public static BusinessException unauthorized(String message) {
        return new BusinessException(
                "UNAUTHORIZED",
                "Требуется авторизация",
                message,
                HttpStatus.UNAUTHORIZED
        );
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(
                "FORBIDDEN",
                "Доступ запрещен",
                message,
                HttpStatus.FORBIDDEN
        );
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(
                "BAD_REQUEST",
                "Некорректный запрос",
                message,
                HttpStatus.BAD_REQUEST
        );
    }

    public static BusinessException internalError(String message) {
        return new BusinessException(
                "INTERNAL_ERROR",
                "Внутренняя ошибка сервера",
                message,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}