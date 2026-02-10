package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ResourceNotFoundException extends BusinessException {
    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(
                "RESOURCE_NOT_FOUND",
                "Ресурс не найден",
                String.format("%s с идентификатором '%s' не найден", resourceType, resourceId),
                HttpStatus.NOT_FOUND,
                Map.of(
                        "resourceType", resourceType,
                        "resourceId", resourceId
                )
        );
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
}