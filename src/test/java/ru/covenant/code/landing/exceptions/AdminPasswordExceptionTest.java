package ru.covenant.code.landing.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPasswordExceptionTest {

    @Test
    void constructorShouldSetCorrectFields() {
        String message = "Test message";
        AdminPasswordException ex = new AdminPasswordException(message);

        assertThat(ex).isInstanceOf(BusinessException.class);
        assertThat(ex.getErrorCode()).isEqualTo("ADMIN_PASSWORD_ERROR");
        assertThat(ex.getDescription()).isEqualTo("Ошибка пароля администратора");
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getDetails()).isNull();
    }

    @Test
    void wrongCurrentPasswordShouldCreateCorrectException() {
        AdminPasswordException ex = AdminPasswordException.wrongCurrentPassword();

        assertThat(ex.getMessage()).isEqualTo("Текущий пароль неверен");
        assertThat(ex.getErrorCode()).isEqualTo("ADMIN_PASSWORD_ERROR");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void passwordsNotMatchShouldCreateCorrectException() {
        AdminPasswordException ex = AdminPasswordException.passwordsNotMatch();

        assertThat(ex.getMessage()).isEqualTo("Новые пароли не совпадают");
        assertThat(ex.getErrorCode()).isEqualTo("ADMIN_PASSWORD_ERROR");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void sameAsOldShouldCreateCorrectException() {
        AdminPasswordException ex = AdminPasswordException.sameAsOld();

        assertThat(ex.getMessage()).isEqualTo("Новый пароль должен отличаться от старого");
        assertThat(ex.getErrorCode()).isEqualTo("ADMIN_PASSWORD_ERROR");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}