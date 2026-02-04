package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Ответ на создание заявки клиента")
public class ClientsCreateRsDto {

    @Schema(
            description = "Уникальный идентификатор созданной заявки",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "Имя клиента",
            example = "Иван"
    )
    private String name;

    @Schema(
            description = "Email клиента",
            example = "ivan@example.com"
    )
    private String email;

    @Schema(
            description = "Тип курса (строковое представление)",
            example = "PYTHON_BASIC"
    )
    private String courseType;

    @Schema(
            description = "Статус созданной заявки",
            example = "NEW"
    )
    private Status status;

    @Schema(
            description = "Дата и время создания заявки",
            example = "2024-01-15T14:30:00+03:00"
    )
    private OffsetDateTime createdAt;

    @Schema(
            description = "Сообщение для клиента",
            example = "Заявка успешно создана. Мы свяжемся с вами в ближайшее время!"
    )
    private String message = "Заявка успешно создана. Мы свяжемся с вами в ближайшее время!";
}