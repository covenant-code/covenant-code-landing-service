package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ClientDuplicateException extends BusinessException {
    private final String email;
    private final String phone;

    public ClientDuplicateException(String email, String phone) {
        super(
                "CLIENT_DUPLICATE",
                "Дубликат заявки",
                String.format("Заявка с email '%s' или телефоном '%s' уже существует", email, phone),
                HttpStatus.CONFLICT,
                Map.of(
                        "email", email,
                        "phone", phone
                )
        );
        this.email = email;
        this.phone = phone;
    }
}
