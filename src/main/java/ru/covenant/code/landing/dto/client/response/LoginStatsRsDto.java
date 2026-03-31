package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Статистика страницы входа")
public class LoginStatsRsDto {

    @Schema(
            description = "Количество заявок за сегодня",
            example = "15"
    )
    private long todayApplications;

    @Schema(
            description = "Процент успешных заявок",
            example = "85"
    )
    private long successRate;

    @Schema(
            description = "Общее количество заявок",
            example = "1250"
    )
    private long totalApplications;

    @Schema(
            description = "Количество успешных заявок",
            example = "1063"
    )
    private long successfulApplications;
}