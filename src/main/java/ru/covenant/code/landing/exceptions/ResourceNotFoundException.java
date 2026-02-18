package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceNotFoundException extends BusinessException {

    String resourceType;
    String resourceId;

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
