package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AdminPasswordException extends BusinessException {
    public AdminPasswordException(String message) {
        super(
                "ADMIN_PASSWORD_ERROR",
                "Ошибка пароля администратора",
                message,
                HttpStatus.BAD_REQUEST,
                null
        );
    }

    public static AdminPasswordException wrongCurrentPassword() {
        return new AdminPasswordException("Текущий пароль неверен");
    }

    public static AdminPasswordException passwordsNotMatch() {
        return new AdminPasswordException("Новые пароли не совпадают");
    }

    public static AdminPasswordException sameAsOld() {
        return new AdminPasswordException("Новый пароль должен отличаться от старого");
    }
}
