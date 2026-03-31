package ru.covenant.code.landing.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BusinessException extends RuntimeException {

    String errorCode;
    String description;
    String message;
    HttpStatus httpStatus;
    Object details;

    public BusinessException(String errorCode, String description, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.description = description;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorCode, String description, String message, HttpStatus httpStatus, Object details) {
        this(errorCode, description, message, httpStatus);
        this.details = details;
    }
}
