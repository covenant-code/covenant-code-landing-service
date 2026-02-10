package ru.covenant.code.landing.entity.enumerated;

public enum CourseType {
    FULLSTACK("Fullstack разработка"),
    FRONTEND("Frontend разработка"),
    BACKEND("Backend разработка");

    private final String displayName;

    CourseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}