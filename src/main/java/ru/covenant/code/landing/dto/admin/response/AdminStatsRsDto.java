package ru.covenant.code.landing.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Статистика по администраторам")
public class AdminStatsRsDto {

    @Schema(
            description = "Общее количество администраторов",
            example = "25"
    )
    private long totalAdmins;

    @Schema(
            description = "Количество активных администраторов",
            example = "20"
    )
    private long activeAdmins;

    @Schema(
            description = "Количество неактивных администраторов",
            example = "5"
    )
    private long inactiveAdmins;

    @Schema(
            description = "Распределение администраторов по ролям",
            example = "{\"ADMIN\": 10, \"MODERATOR\": 8, \"SUPPORT\": 7}"
    )
    private Map<String, Long> adminsByRole;

    @Schema(
            description = "Распределение администраторов по отделам",
            example = "{\"Техническая поддержка\": 15, \"Маркетинг\": 5, \"Продажи\": 5}"
    )
    private Map<String, Long> adminsByDepartment;
}