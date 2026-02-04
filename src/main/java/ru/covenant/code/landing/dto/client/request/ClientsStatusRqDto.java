package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.covenant.code.landing.validation.constraints.*;

@Getter
@Setter
@Schema(description = "Запрос на изменение статуса клиента")
public class ClientsStatusRqDto {

    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.Status.class,
            message = "Недопустимый статус. Допустимые значения: NEW, PROCESSED, DONE"
    )
    @Schema(
            description = "Новый статус клиента",
            example = "PROCESSED",
            required = true,
            allowableValues = {"NEW", "PROCESSED", "DONE"}
    )
    private String status;

    @Schema(
            description = "Имя администратора, обработавшего заявку",
            example = "admin@covenantcode.ru"
    )
    private String processedBy;

    public ru.covenant.code.landing.entity.enumerated.Status getStatusEnum() {
        if (status == null) {
            return null;
        }
        try {
            return ru.covenant.code.landing.entity.enumerated.Status.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}