package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter @Setter
public class ClientDuplicateException extends BusinessException {

    public final String email;
    public final String phone;

    public ClientDuplicateException(String email, String phone) {
        super("CLIENT_DUPLICATE",
                "Дубликат заявки",
                "Заявка с email '%s' или телефоном '%s' уже существует".formatted(email, phone),
                HttpStatus.CONFLICT,
                Map.of("phone", phone,
                        "email", email));
        this.email = email;
        this.phone = phone;
    }
}
