package ru.covenant.code.landing.entity.enumerated;

public enum Priority {
    HIGH("Высокий"),
    MEDIUM("Средний"),
    LOW("Низкий");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
