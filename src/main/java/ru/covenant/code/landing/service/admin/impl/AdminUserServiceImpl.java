package ru.covenant.code.landing.service.admin.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.exceptions.ExceptionFactory;
import ru.covenant.code.landing.repository.AdminUserRepository;
import ru.covenant.code.landing.service.admin.AdminUserService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public void updateLastLogin(String email) {
        log.info("updateLastLogin");

        try {
            AdminUser user = adminUserRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("Пользователь с email {} не найден", email);
                        return ExceptionFactory.adminNotFound(null);
                    });
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            log.debug("Установлено время последнего входа для пользователя");

            adminUserRepository.save(user);

        } catch (Exception e) {
            log.debug("Ошибка при обновлении времени последнего входа для пользователя", email, e.getMessage(), e);
        }
    }
}
