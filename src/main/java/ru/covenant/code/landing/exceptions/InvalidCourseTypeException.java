package ru.covenant.code.landing.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCourseTypeException extends RuntimeException {
    public InvalidCourseTypeException(String message) {
        super(message);
    }
}
