package ru.covenant.code.landing.entity.enumerated;

public enum Status {
    NEW("Новые"),
    PROCESSED("В обработке"),
    DONE("Обработано");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
