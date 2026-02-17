package ru.covenant.code.landing.entity.enumerated;

public enum AdminRole {
    SUPER_ADMIN("Супер администратор"),
    ADMIN("Администратор"),
    MODERATOR("Модератор"),
    SUPPORT("Поддержка");

    private final String description;


    AdminRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
