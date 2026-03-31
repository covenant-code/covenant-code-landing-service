package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static java.lang.String.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminEmailDuplicateException extends BusinessException {

    String email;

    public AdminEmailDuplicateException(String email) {
        super("ADMIN_EMAIL_DUPLICATE", "Email уже используется", format("Администратор с email '{%s}' уже существует", email),
                HttpStatus.CONFLICT, Map.of("email", email));
        this.email = email;
    }
}
