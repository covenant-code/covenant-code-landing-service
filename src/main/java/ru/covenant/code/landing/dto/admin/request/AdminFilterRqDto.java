package ru.covenant.code.landing.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Фильтр для поиска администраторов")
public class AdminFilterRqDto {

    @Schema(
            description = "Поиск по имени, фамилии или email",
            example = "Иван"
    )
    private String search;

    @Schema(
            description = "Фильтр по роли",
            example = "ADMIN",
            allowableValues = {"SUPER_ADMIN", "ADMIN", "MODERATOR", "SUPPORT"}
    )
    private String role;

    @Schema(
            description = "Фильтр по активности",
            example = "true"
    )
    private Boolean active;

    @Schema(
            description = "Фильтр по отделу/департаменту",
            example = "Техническая поддержка"
    )
    private String department;

    @Schema(
            description = "Поле для сортировки",
            example = "createdAt",
            allowableValues = {"createdAt", "lastLoginAt", "firstName", "lastName", "email"}
    )
    private String sortBy;

    @Schema(
            description = "Направление сортировки",
            example = "desc",
            allowableValues = {"asc", "desc"}
    )
    private String sortOrder;
}