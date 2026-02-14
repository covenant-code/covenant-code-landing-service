package ru.covenant.code.landing.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
    @Test
    void securityConfig_hasRequiredAnnotations() {
        assertNotNull(SecurityConfig.class.getAnnotation(Configuration.class), "@Configuration missing");
        assertNotNull(SecurityConfig.class.getAnnotation(EnableWebSecurity.class), "@EnableWebSecurity missing");
        assertNotNull(SecurityConfig.class.getAnnotation(EnableMethodSecurity.class), "@EnableMethodSecurity missing");
    }

    @Test
    void passwordEncoderBean_returnsBCrypt() {
        SecurityConfig config = new SecurityConfig();
        var encoder = config.passwordEncoder();

        assertNotNull(encoder, "PasswordEncoder is null");
        assertEquals(BCryptPasswordEncoder.class, encoder.getClass(),
                "PasswordEncoder must be BCryptPasswordEncoder");
    }

    @Test
    void passwordEncoderMethod_isAnnotatedWithBean() throws NoSuchMethodException {
        Method m = SecurityConfig.class.getDeclaredMethod("passwordEncoder");
        assertNotNull(m.getAnnotation(Bean.class), "passwordEncoder() must be annotated with @Bean");
    }

    @Test
    void authenticationManagerMethod_isAnnotatedWithBean() throws NoSuchMethodException {
        Method m = SecurityConfig.class.getDeclaredMethod(
                "authenticationManager",
                org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration.class
        );
        assertNotNull(m.getAnnotation(Bean.class), "authenticationManager(...) must be annotated with @Bean");
    }

    @Test
    void securityFilterChainMethod_isAnnotatedWithBean() throws NoSuchMethodException {
        Method m = SecurityConfig.class.getDeclaredMethod(
                "securityFilterChain",
                org.springframework.security.config.annotation.web.builders.HttpSecurity.class
        );
        assertNotNull(m.getAnnotation(Bean.class), "securityFilterChain(HttpSecurity) must be annotated with @Bean");
    }
}