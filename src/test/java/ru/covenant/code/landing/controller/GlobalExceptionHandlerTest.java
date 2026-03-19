package ru.covenant.code.landing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.covenant.code.landing.controller.advice.GlobalExceptionHandler;
import ru.covenant.code.landing.error.ErrorResponse;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.AdminNotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusinessException_withAdminNotFoundException_shouldReturnNotFoundStatusAndCorrectErrorResponse() {
        UUID adminId = UUID.randomUUID();
        AdminNotFoundException exception = new AdminNotFoundException(adminId);

        ResponseEntity<ResponseWrapper<Void>> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();

        ErrorResponse error = response.getBody().getError();
        assertThat(error.getCode()).isEqualTo("ADMIN_NOT_FOUND");
        assertThat(error.getDescription()).isEqualTo("Администратор не найден");
        assertThat(error.getMessage()).isEqualTo("Администратор с ID {" + adminId + "} не найден");
    }

    @Test
    void handleValidationExceptions_withMultipleErrors_shouldReturnBadRequestWithValidationErrorDetails() {
        // given
        Object target = new Object();
        String objectName = "testDto";
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);
        bindingResult.addError(new FieldError(objectName, "username", "must not be blank"));
        bindingResult.addError(new FieldError(objectName, "password", "size must be between 6 and 20"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ResponseEntity<ResponseWrapper<Void>> response = exceptionHandler.handleValidationExceptions(ex);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();

        ErrorResponse error = response.getBody().getError();
        assertThat(error.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(error.getDescription()).isEqualTo("Ошибка валидации данных");
        assertThat(error.getMessage()).isEqualTo("Проверьте правильность заполнения полей");
        assertThat(error.getDetails()).isNotNull();
        assertThat(error.getDetails()).isInstanceOf(Map.class);

        Map<String, String> details = (Map<String, String>) error.getDetails();
        assertThat(details)
                .containsEntry("username", "must not be blank")
                .containsEntry("password", "size must be between 6 and 20")
                .hasSize(2);
    }

    @Test
    void handleConstraintViolationException_withMultipleViolations_shouldReturnBadRequestWithValidationErrorDetails() {

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        when(path1.toString()).thenReturn("username");
        when(path2.toString()).thenReturn("email");

        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation1.getMessage()).thenReturn("must not be blank");
        when(violation2.getMessage()).thenReturn("must be a valid email");

        Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        ResponseEntity<ResponseWrapper<Void>> response = exceptionHandler.handleConstraintViolationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();

        ErrorResponse error = response.getBody().getError();
        assertThat(error.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(error.getDescription()).isEqualTo("Ошибка валидации данных");
        assertThat(error.getMessage()).isEqualTo("Проверьте правильность заполнения полей");
        assertThat(error.getDetails()).isNotNull();
        assertThat(error.getDetails()).isInstanceOf(Map.class);

        Map<String, String> details = (Map<String, String>) error.getDetails();
        assertThat(details)
                .containsEntry("username", "must not be blank")
                .containsEntry("email", "must be a valid email")
                .hasSize(2);
    }

    @Test
    void handleGenericException_withRuntimeException_shouldReturnInternalServerErrorWithErrorDetails() {

        RuntimeException ex = new RuntimeException("Test exception message");

        ResponseEntity<ResponseWrapper<Void>> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();

        ErrorResponse error = response.getBody().getError();
        assertThat(error.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(error.getDescription()).isEqualTo("Внутренняя ошибка сервера");
        assertThat(error.getMessage()).isEqualTo("Произошла непредвиденная ошибка");
        assertThat(error.getDetails()).isNotNull();
        assertThat(error.getDetails()).isInstanceOf(Map.class);

        Map<String, String> details = (Map<String, String>) error.getDetails();
        assertThat(details)
                .containsEntry("exceptionClass", "java.lang.RuntimeException")
                .containsEntry("exceptionMessage", "Test exception message")
                .hasSize(2);
    }

    @Test
    void allExceptionHandlers_shouldReturnConsistentResponseStructure() {
        AdminNotFoundException adminEx = new AdminNotFoundException(UUID.randomUUID());
        ResponseEntity<ResponseWrapper<Void>> response1 = exceptionHandler.handleBusinessException(adminEx);
        assertCommonResponseStructure(response1, HttpStatus.NOT_FOUND, "ADMIN_NOT_FOUND");


        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "dto");
        bindingResult.addError(new FieldError("dto", "field", "error"));
        MethodArgumentNotValidException validEx = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);
        ResponseEntity<ResponseWrapper<Void>> response2 = exceptionHandler.handleValidationExceptions(validEx);
        assertCommonResponseStructure(response2, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("error");
        ConstraintViolationException constrEx = new ConstraintViolationException("Validation failed", Set.of(violation));
        ResponseEntity<ResponseWrapper<Void>> response3 = exceptionHandler.handleConstraintViolationException(constrEx);
        assertCommonResponseStructure(response3, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");

        RuntimeException genericEx = new RuntimeException("test");
        ResponseEntity<ResponseWrapper<Void>> response4 = exceptionHandler.handleGenericException(genericEx);
        assertCommonResponseStructure(response4, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    private void assertCommonResponseStructure(ResponseEntity<ResponseWrapper<Void>> response, HttpStatus expectedStatus, String expectedCode) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();
        assertThat(response.getBody().getError().getCode()).isEqualTo(expectedCode);
        assertThat(response.getBody().getError().getDescription()).isNotEmpty();
        assertThat(response.getBody().getError().getMessage()).isNotEmpty();
    }
}