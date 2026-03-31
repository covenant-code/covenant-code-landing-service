package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@Schema(description = "Информация о курсе")
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)//все поля приватные
@AllArgsConstructor
public class CourseInfoDto {
        @Schema(description = "Код курса (значение Enum)", example = "BACKEND")
        String code;

        @Schema(description = "Название курса (displayName)", example = "Backend разработка")
        String name;

        @Schema(description = "Описание курса", example = "Разработка серверной логики и API")
        String description;

        @Schema(description = "Длительность обучения", example = "5 месяцев")
        String duration;

        @Schema(description = "Стоимость курса", example = "30 000 ₽")
        String price;
 }
