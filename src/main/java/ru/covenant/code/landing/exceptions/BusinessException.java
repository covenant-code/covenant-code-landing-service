package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String description;
    private final HttpStatus httpStatus;
    private final Object details;

    public BusinessException(String errorCode, String description, String message, HttpStatus httpStatus) {
        this(errorCode, description, message, httpStatus, null);
    }

    public BusinessException(String errorCode, String description, String message, HttpStatus httpStatus, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.description = description;
        this.httpStatus = httpStatus;
        this.details = details;
    }
}