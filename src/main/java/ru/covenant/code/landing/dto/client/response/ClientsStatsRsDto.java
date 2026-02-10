package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Статистика по клиентам")
public class ClientsStatsRsDto {

    @Schema(
            description = "Общее количество заявок",
            example = "1250"
    )
    private long total;

    @Schema(
            description = "Количество новых заявок",
            example = "15"
    )
    private long newCount;

    @Schema(
            description = "Количество заявок в обработке",
            example = "25"
    )
    private long processedCount;

    @Schema(
            description = "Количество обработанных заявок",
            example = "1200"
    )
    private long doneCount;

    @Schema(
            description = "Количество заявок за сегодня",
            example = "15"
    )
    private long todayCount;

    @Schema(
            description = "Количество заявок на Fullstack курс",
            example = "500"
    )
    private long fullstackCount;

    @Schema(
            description = "Количество заявок на Frontend курс",
            example = "350"
    )
    private long frontendCount;

    @Schema(
            description = "Количество заявок на Backend курс",
            example = "400"
    )
    private long backendCount;

    @Schema(
            description = "Количество заявок с высоким приоритетом",
            example = "100"
    )
    private long highPriorityCount;

    @Schema(
            description = "Количество заявок со средним приоритетом",
            example = "800"
    )
    private long mediumPriorityCount;

    @Schema(
            description = "Количество заявок с низким приоритетом",
            example = "350"
    )
    private long lowPriorityCount;
}