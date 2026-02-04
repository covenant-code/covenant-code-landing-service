package ru.covenant.code.landing.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessageDto {
    private String type;
    private Object application;      // для APPLICATION_CREATED/UPDATED
    private String applicationId;    // для APPLICATION_DELETED
    private Object stats;            // для STATS_UPDATED

    // Конструкторы
    public WebSocketMessageDto() {}

    public WebSocketMessageDto(String type, Object application) {
        this.type = type;
        this.application = application;
    }

    public WebSocketMessageDto(String type, String applicationId) {
        this.type = type;
        this.applicationId = applicationId;
    }

    // Геттеры и сеттеры
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Object getApplication() { return application; }
    public void setApplication(Object application) { this.application = application; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public Object getStats() { return stats; }
    public void setStats(Object stats) { this.stats = stats; }
}