package ru.covenant.code.landing.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessageDto {
    private String type;

    private Object application;

    private String applicationId;

    private Object stats;

    public WebSocketMessageDto() {
    }

    public WebSocketMessageDto(String type, Object application) {
        this.type = type;
        this.application = application;
    }

    public WebSocketMessageDto(String type, String applicationId) {
        this.type = type;
        this.applicationId = applicationId;
    }
}
