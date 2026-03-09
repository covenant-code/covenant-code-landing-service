package ru.covenant.code.landing.service.admin.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.dto.admin.request.AdminLoginRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminLoginRsDto;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.ExceptionFactory;
import ru.covenant.code.landing.repository.AdminUserRepository;
import ru.covenant.code.landing.service.admin.AdminUserService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AdminLoginRsDto updateLastLogin(AdminLoginRqDto loginRequest) {
        log.info("updateLastLogin");

        try {
            AdminUser user = adminUserRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.error("Пользователь с email {} не найден", loginRequest.getEmail());
                        return ExceptionFactory.adminNotFound(null);
                    });
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            log.debug("Установлено время последнего входа для пользователя");

            adminUserRepository.save(user);

            AdminLoginRsDto adminLoginRsDto = buildAuthResponse(loginRequest);

            return adminLoginRsDto;
        } catch (Exception e) {
            log.debug("Ошибка при обновлении времени последнего входа для пользователя", loginRequest.getEmail(), e.getMessage(), e);
            return new AdminLoginRsDto();
        }
    }

    private AdminLoginRsDto buildAuthResponse(AdminLoginRqDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(auth -> auth.replace("ROLE_", ""))
                .orElse("ADMIN");

        AdminLoginRsDto response = AdminLoginRsDto.builder()
                .success(true)
                .message("Авторизация успешна")
                .authenticated(true)
                .email(loginRequest.getEmail())
                .role(role)
                .build();
        return response;
    }
}
