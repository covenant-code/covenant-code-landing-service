package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Ответ на создание заявки клиента")
public class ClientsCreateRsDto {

    @Schema(description = "Идентификатор заявки")
    private UUID id;

    @Schema(description = "Имя клиента", example = "Иван Иванов")
    private String name;

    @Schema(description = "Email клиента", example = "ivan@example.com")
    private String email;

    @Schema(description = "Тип курса", example = "BACKEND")
    private CourseType courseType;

    @Schema(description = "Статус заявки", example = "NEW")
    private Status status;

    @Schema(description = "Дата создания заявки")
    private OffsetDateTime createdAt;

    @Schema(description = "Сообщение для клиента", example = "Заявка успешно создана. Мы свяжемся с вами в ближайшее время!")
    private String message = "Заявка успешно создана. Мы свяжемся с вами в ближайшее время!";
}
