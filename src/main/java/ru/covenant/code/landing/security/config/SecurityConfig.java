package ru.covenant.code.landing.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Публичные endpoints (без аутентификации)
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/login.html"
                        ).permitAll()

                        // Статические ресурсы
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/logo.svg",
                                "/favicon.ico",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Swagger/OpenAPI документация
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/swagger-ui/index.html",
                                "/swagger-ui/swagger-initializer.js",
                                "/swagger-ui/swagger-ui.css",
                                "/swagger-ui/swagger-ui-bundle.js",
                                "/swagger-ui/swagger-ui-standalone-preset.js",
                                "/swagger-ui/favicon-32x32.png",
                                "/swagger-ui/favicon-16x16.png"
                        ).permitAll()

                        // Публичные API endpoints
                        .requestMatchers(
                                "/api/admin/login",
                                "/actuator/health",
                                "/error",
                                "/api/v1/clients/login/stats",
                                "/api/v1/clients",
                                "/api/v1/clients/courses"
                        ).permitAll()

                        // WebSocket endpoints
                        .requestMatchers(
                                "/ws/**",
                                "/topic/**",
                                "/app/**"
                        ).permitAll()

                        // Тестовые endpoints
                        .requestMatchers(
                                "/api/test/**",
                                "/api/log/**"
                        ).permitAll()

                        // Административные endpoints (требуют аутентификации)
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/v1/admin/**"
                        ).authenticated()

                        // Управление пользователями (только для ADMIN и SUPER_ADMIN)
                        .requestMatchers("/api/v1/admin/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // Управление клиентами (разные роли)
                        .requestMatchers(
                                "/api/v1/admin/clients/**",
                                "/admin/clients/**"
                        ).hasAnyRole("SUPER_ADMIN", "ADMIN", "MODERATOR", "SUPPORT")

                        // Административная панель
                        .requestMatchers(
                                "/admin",
                                "/admin/**",
                                "/admin.html"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/admin/login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                        .ignoringRequestMatchers(
                                // Swagger endpoints
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**",

                                // Публичные API
                                "/api/admin/login",
                                "/actuator/health",
                                "/api/v1/clients",
                                "/api/v1/clients/courses",
                                "/api/v1/clients/login/stats",

                                // WebSocket
                                "/ws/**",
                                "/topic/**",
                                "/app/**",

                                // Тестовые endpoints
                                "/api/test/**",
                                "/api/log/**"
                        )
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }
}