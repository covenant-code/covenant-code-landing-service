package ru.covenant.code.landing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.repository.AdminUserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Попытка загрузки пользователя: {}", username);

        AdminUser adminUser = adminUserRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        // Проверка активности пользователя
        if (!adminUser.isActive()) {
            log.warn("Учетная запись заблокирована: {}", username);
            throw new DisabledException("Учетная запись заблокирована");
        }

        log.debug("Пользователь найден: {} (роль: {})", username, adminUser.getRole());

        // Создаем список ролей с префиксом ROLE_
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + adminUser.getRole().name())
        );

        return User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!adminUser.isActive())
                .credentialsExpired(false)
                .disabled(!adminUser.isActive())
                .build();
    }
}