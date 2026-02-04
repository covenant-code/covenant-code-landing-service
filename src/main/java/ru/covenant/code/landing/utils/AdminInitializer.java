package ru.covenant.code.landing.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.entity.enumerated.AdminRole;
import ru.covenant.code.landing.repository.AdminUserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminIfNotExists("admin@covenantcode.ru", "admin123", "Admin", "User", AdminRole.ADMIN);
        createAdminIfNotExists("super@covenantcode.ru", "admin123", "Super", "Admin", AdminRole.SUPER_ADMIN);
    }

    private void createAdminIfNotExists(String email, String password, String firstName, String lastName, AdminRole role) {
        if (!adminUserRepository.existsByEmail(email)) {
            String encodedPassword = passwordEncoder.encode(password);

            log.info("Creating admin user: {} with password hash: {}", email, encodedPassword);

            AdminUser admin = AdminUser.builder()
                    .email(email)
                    .password(encodedPassword)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .active(true)
                    .build();

            adminUserRepository.save(admin);
            log.info("Admin user created: {}", email);
        } else {
            log.info("Admin user already exists: {}", email);

            // Проверим и обновим пароль если нужно
            adminUserRepository.findByEmail(email).ifPresent(admin -> {
                String currentPassword = admin.getPassword();
                if (!passwordEncoder.matches(password, currentPassword)) {
                    log.info("Updating password for user: {}", email);
                    admin.setPassword(passwordEncoder.encode(password));
                    adminUserRepository.save(admin);
                }
            });
        }
    }
}