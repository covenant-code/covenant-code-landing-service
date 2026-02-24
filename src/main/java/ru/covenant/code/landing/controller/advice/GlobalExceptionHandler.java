package ru.covenant.code.landing.controller.advice;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.covenant.code.landing.error.ErrorResponse;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.BusinessException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@CommonsLog
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleBusinessException(BusinessException ex) {
        ErrorResponse error = ErrorResponse.of(
                ex.getErrorCode(),
                ex.getDescription(),
                ex.getMessage(),
                ex.getDetails()
        );
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ResponseWrapper.error(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Ошибка валидации данных",
                "Проверьте правильность заполнения полей",
                errors
        );
        return ResponseEntity
                .badRequest()
                .body(ResponseWrapper.error(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));

        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Ошибка валидации данных",
                "Проверьте правильность заполнения полей",
                errors
        );
        return ResponseEntity
                .badRequest()
                .body(ResponseWrapper.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGenericException(Exception ex) {
        log.error("Непредвиденная ошибка: ", ex);

        Map<String, String> details = new HashMap<>();
        details.put("exceptionClass", ex.getClass().getName());
        details.put("exceptionMessage", ex.getMessage() != null ? ex.getMessage() : "No message");

        ErrorResponse error = ErrorResponse.of(
                "INTERNAL_ERROR",
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка",
                details
        );
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error(error));
    }
}