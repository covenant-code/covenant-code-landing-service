package ru.covenant.code.landing.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionFactoryTest {

    //исключения клиента

    @Test
    void testClientNotFound() {
        UUID clientId = UUID.randomUUID();
        BusinessException exception = ExceptionFactory.clientNotFound(clientId);

        assertThat(exception).isInstanceOf(ClientNotFoundException.class);
        assertThat(exception.getErrorCode()).isEqualTo("CLIENT_NOT_FOUND");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        ClientNotFoundException clientEx = (ClientNotFoundException) exception;
        assertThat(clientEx.getClientId()).isEqualTo(clientId);
    }

    @Test
    void testInvalidClientStatus() {
        String currentStatus = "ACTIVE";
        String attemptedStatus = "BLOCKED";

        BusinessException exception = ExceptionFactory.invalidClientStatus(currentStatus, attemptedStatus);

        assertThat(exception).isInstanceOf(InvalidClientStatusException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage())
                .contains(currentStatus)
                .contains(attemptedStatus);
    }

    @Test
    void testClientDuplicate() {
        String email = "test@example.com";
        String phone = "+1234567890";

        BusinessException exception = ExceptionFactory.clientDuplicate(email, phone);

        assertThat(exception).isInstanceOf(ClientDuplicateException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage())
                .contains(email)
                .contains(phone);
    }

    //админские тесты
    @Test
    void testAdminNotFound() {
        UUID adminId = UUID.randomUUID();
        BusinessException exception = ExceptionFactory.adminNotFound(adminId);

        assertThat(exception).isInstanceOf(AdminNotFoundException.class);
        assertThat(exception.getErrorCode()).isEqualTo("ADMIN_NOT_FOUND");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).contains(adminId.toString());
    }

    @Test
    void testAdminEmailDuplicate() {
        String email = "admin@example.com";

        BusinessException exception = ExceptionFactory.adminEmailDuplicate(email);

        assertThat(exception).isInstanceOf(AdminEmailDuplicateException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(email);
    }

    @Test
    void testAdminPermissionDenied() {
        String requiredRole = "ADMIN";
        String currentRole = "USER";

        BusinessException exception = ExceptionFactory.adminPermissionDenied(requiredRole, currentRole);

        assertThat(exception).isInstanceOf(AdminPermissionException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getMessage())
                .contains(requiredRole)
                .contains(currentRole);
    }

    @Test
    void testAdminPasswordExceptions() {
        BusinessException wrongPass = ExceptionFactory.adminPasswordWrong();
        BusinessException notMatch = ExceptionFactory.adminPasswordsNotMatch();
        BusinessException sameAsOld = ExceptionFactory.adminPasswordSameAsOld();

        assertThat(wrongPass).isInstanceOf(AdminPasswordException.class);
        assertThat(notMatch).isInstanceOf(AdminPasswordException.class);
        assertThat(sameAsOld).isInstanceOf(AdminPasswordException.class);

        assertThat(wrongPass.getMessage()).contains("неверен");
        assertThat(notMatch.getMessage()).contains("не совпадают");
        assertThat(sameAsOld.getMessage()).contains("отличаться");

        assertThat(wrongPass.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    //исключения
    @Test
    void testValidationError() {
        String[] messages = {
                "Поле email обязательно",
                "Некорректный формат телефона",
                "Пароль слишком короткий"
        };

        for (String message : messages) {
            BusinessException exception = ExceptionFactory.validationError(message);

            assertThat(exception).isInstanceOf(ValidationException.class);
            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getMessage()).contains(message);
        }
    }

    @Test
    void testNotFound() {
        Map<String, String> resources = Map.of(
                "Client", UUID.randomUUID().toString(),
                "Admin", "admin-123",
                "Order", "ORDER-001"
        );

        resources.forEach((type, id) -> {
            BusinessException exception = ExceptionFactory.notFound(type, id);

            assertThat(exception).isInstanceOf(ResourceNotFoundException.class);
            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(exception.getMessage())
                    .contains(type)
                    .contains(id);
        });
    }

    @Test
    void testPersistenceError() {
        String entity = "Order";
        List<String> operations = List.of("save", "update", "delete", "find", "validate");
        Throwable cause = new RuntimeException("DB error");

        for (String operation : operations) {
            BusinessException exception = ExceptionFactory.persistenceError(entity, operation, cause);

            assertThat(exception)
                    .as("Проверка операции: %s", operation)
                    .isInstanceOf(PersistenceException.class);
            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(exception.getMessage())
                    .contains(entity)
                    .contains(operation);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    //быстрые методы
    @Test
    void testQuickCreationMethods() {
        String message = "Test message";

        BusinessException unauthorized = ExceptionFactory.unauthorized(message);
        BusinessException forbidden = ExceptionFactory.forbidden(message);
        BusinessException badRequest = ExceptionFactory.badRequest(message);
        BusinessException internalError = ExceptionFactory.internalError(message);

        assertThat(unauthorized.getErrorCode()).isEqualTo("UNAUTHORIZED");
        assertThat(unauthorized.getDescription()).isEqualTo("Не авторизован");
        assertThat(unauthorized.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(unauthorized.getDetails()).isEqualTo(message);

        assertThat(forbidden.getErrorCode()).isEqualTo("FORBIDDEN");
        assertThat(forbidden.getDescription()).isEqualTo("Доступ запрещен");
        assertThat(forbidden.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(forbidden.getDetails()).isEqualTo(message);

        assertThat(badRequest.getErrorCode()).isEqualTo("BAD_REQUEST");
        assertThat(badRequest.getDescription()).isEqualTo("Некорректный запрос");
        assertThat(badRequest.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(badRequest.getDetails()).isEqualTo(message);

        assertThat(internalError.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(internalError.getDescription()).isEqualTo("Ошибка сервера");
        assertThat(internalError.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(internalError.getDetails()).isEqualTo(message);
    }

    //проверка деталей исключения
    @Test
    void testExceptionDetails() {
        UUID adminId = UUID.randomUUID();
        AdminNotFoundException exception = (AdminNotFoundException) ExceptionFactory.adminNotFound(adminId);

        Map<String, String> details = (Map<String, String>) exception.getDetails();
        assertThat(details).containsEntry("adminId", adminId.toString());
    }


}