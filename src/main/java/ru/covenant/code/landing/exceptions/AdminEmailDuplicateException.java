package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class AdminEmailDuplicateException extends BusinessException {
    private final String email;

    public AdminEmailDuplicateException(String email) {
        super(
                "ADMIN_EMAIL_DUPLICATE",
                "Email уже используется",
                String.format("Администратор с email '%s' уже существует", email),
                HttpStatus.CONFLICT,
                Map.of("email", email)
        );
        this.email = email;
    }
}
