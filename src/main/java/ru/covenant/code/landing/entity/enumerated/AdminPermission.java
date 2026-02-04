package ru.covenant.code.landing.entity.enumerated;

import lombok.Getter;

@Getter
public enum AdminPermission {
    // Управление клиентами
    CLIENT_VIEW("Просмотр клиентов"),
    CLIENT_EDIT("Редактирование клиентов"),
    CLIENT_DELETE("Удаление клиентов"),

    // Управление администраторами
    ADMIN_VIEW("Просмотр администраторов"),
    ADMIN_CREATE("Создание администраторов"),
    ADMIN_EDIT("Редактирование администраторов"),
    ADMIN_DELETE("Удаление администраторов"),

    // Аналитика
    ANALYTICS_VIEW("Просмотр аналитики"),

    // Настройки
    SETTINGS_EDIT("Редактирование настроек");

    private final String description;

    AdminPermission(String description) {
        this.description = description;
    }
}