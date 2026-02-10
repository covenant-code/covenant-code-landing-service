package ru.covenant.code.landing.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.admin.request.AdminCreateRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminFilterRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminPasswordRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminUpdateRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminRsDto;
import ru.covenant.code.landing.dto.admin.response.AdminStatsRsDto;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.entity.enumerated.AdminRole;
import ru.covenant.code.landing.exceptions.*;
import ru.covenant.code.landing.mapper.admin.AdminMapper;
import ru.covenant.code.landing.repository.AdminUserRepository;
import ru.covenant.code.landing.service.admin.AdminUserService;
import ru.covenant.code.landing.specification.AdminUserSpecification;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminMapper adminMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminRsDto> getAllAdmins(AdminFilterRqDto filter, Pageable pageable) {
        try {
            return adminUserRepository.findAll(
                    AdminUserSpecification.filter(filter),
                    pageable
            ).map(adminMapper::toDto);
        } catch (Exception e) {
            throw ExceptionFactory.internalError("Ошибка при получении списка администраторов");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRsDto getAdminById(UUID id) {
        return adminUserRepository.findById(id)
                .map(adminMapper::toDto)
                .orElseThrow(() -> ExceptionFactory.adminNotFound(id));
    }


    @Override
    @Transactional
    public AdminRsDto createAdmin(AdminCreateRqDto createDto) {
        try {
            // Проверка уникальности email
            if (adminUserRepository.existsByEmail(createDto.getEmail())) {
                throw ExceptionFactory.adminEmailDuplicate(createDto.getEmail());
            }

            // Проверка совпадения паролей
            if (!createDto.getPassword().equals(createDto.getPassword())) {
                throw ExceptionFactory.adminPasswordsNotMatch();
            }

            // Валидация пароля
            validatePassword(createDto.getPassword());

            AdminUser adminUser = AdminUser.builder()
                    .email(createDto.getEmail())
                    .password(passwordEncoder.encode(createDto.getPassword()))
                    .firstName(createDto.getFirstName())
                    .lastName(createDto.getLastName())
                    .phone(createDto.getPhone())
                    .department(createDto.getDepartment())
                    .role(createDto.getRoleEnum())
                    .active(true)
                    .build();

            AdminUser savedAdmin = adminUserRepository.save(adminUser);
            log.info("Создан новый администратор: {}", savedAdmin.getEmail());

            return adminMapper.toDto(savedAdmin);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.save("администратор", e);
        }
    }

    @Override
    @Transactional
    public AdminRsDto updateAdmin(UUID id, AdminUpdateRqDto updateDto) {
        try {
            AdminUser adminUser = adminUserRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.adminNotFound(id));

            // Обновление email с проверкой уникальности
            if (updateDto.getEmail() != null && !updateDto.getEmail().equals(adminUser.getEmail())) {
                if (adminUserRepository.existsByEmail(updateDto.getEmail())) {
                    throw ExceptionFactory.adminEmailDuplicate(updateDto.getEmail());
                }
                adminUser.setEmail(updateDto.getEmail());
            }

            if (updateDto.getFirstName() != null) {
                adminUser.setFirstName(updateDto.getFirstName());
            }

            if (updateDto.getLastName() != null) {
                adminUser.setLastName(updateDto.getLastName());
            }

            if (updateDto.getPhone() != null) {
                adminUser.setPhone(updateDto.getPhone());
            }

            if (updateDto.getDepartment() != null) {
                adminUser.setDepartment(updateDto.getDepartment());
            }

            if (updateDto.getRole() != null) {
                try {
                    adminUser.setRole(AdminRole.valueOf(updateDto.getRole().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw ExceptionFactory.validationError("Некорректная роль: " + updateDto.getRole());
                }
            }

            if (updateDto.getActive() != null) {
                adminUser.setActive(updateDto.getActive());
            }

            AdminUser updatedAdmin = adminUserRepository.save(adminUser);
            log.info("Обновлен администратор с ID: {}", id);

            return adminMapper.toDto(updatedAdmin);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.update("администратор", e);
        }
    }

    @Override
    @Transactional
    public void deleteAdmin(UUID id) {
        try {
            AdminUser adminUser = adminUserRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.adminNotFound(id));

            // Бизнес-правило: нельзя удалить последнего администратора
            if (adminUserRepository.count() <= 1) {
                throw ExceptionFactory.validationError("Невозможно удалить последнего администратора");
            }

            adminUserRepository.delete(adminUser);
            log.info("Удален администратор с ID: {}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.delete("администратор", e);
        }
    }

    @Override
    @Transactional
    public void changePassword(UUID id, AdminPasswordRqDto passwordDto) {
        try {
            AdminUser adminUser = adminUserRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.adminNotFound(id));

            // Проверка текущего пароля
            if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), adminUser.getPassword())) {
                throw ExceptionFactory.adminPasswordWrong();
            }

            // Проверка совпадения новых паролей
            if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
                throw ExceptionFactory.adminPasswordsNotMatch();
            }

            // Проверка, что новый пароль отличается от старого
            if (passwordEncoder.matches(passwordDto.getNewPassword(), adminUser.getPassword())) {
                throw ExceptionFactory.adminPasswordSameAsOld();
            }

            // Валидация сложности пароля
            validatePassword(passwordDto.getNewPassword());

            adminUser.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
            adminUserRepository.save(adminUser);
            log.info("Изменен пароль администратора с ID: {}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.update("администратор", e);
        }
    }

    @Override
    @Transactional
    public AdminRsDto toggleAdminStatus(UUID id) {
        try {
            AdminUser adminUser = adminUserRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.adminNotFound(id));

            // Бизнес-правило: нельзя деактивировать последнего активного администратора
            if (adminUser.isActive() && adminUserRepository.countActiveAdmins() <= 1) {
                throw ExceptionFactory.validationError("Невозможно деактивировать последнего активного администратора");
            }

            adminUser.setActive(!adminUser.isActive());
            AdminUser updatedAdmin = adminUserRepository.save(adminUser);

            log.info("Изменен статус администратора {} на {}",
                    adminUser.getEmail(), adminUser.isActive() ? "активный" : "неактивный");

            return adminMapper.toDto(updatedAdmin);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.update("администратор", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsRsDto getAdminStats() {
        try {
            AdminStatsRsDto stats = new AdminStatsRsDto();

            stats.setTotalAdmins(adminUserRepository.count());
            stats.setActiveAdmins(adminUserRepository.countActiveAdmins());
            stats.setInactiveAdmins(adminUserRepository.countInactiveAdmins());

            // Статистика по ролям
            Map<String, Long> rolesStats = adminUserRepository.findAll().stream()
                    .collect(Collectors.groupingBy(
                            admin -> admin.getRole().name(),
                            Collectors.counting()
                    ));
            stats.setAdminsByRole(rolesStats);

            // Статистика по отделам (только для тех, у кого указан отдел)
            Map<String, Long> deptStats = adminUserRepository.findAll().stream()
                    .filter(admin -> admin.getDepartment() != null && !admin.getDepartment().isEmpty())
                    .collect(Collectors.groupingBy(
                            AdminUser::getDepartment,
                            Collectors.counting()
                    ));
            stats.setAdminsByDepartment(deptStats);

            return stats;
        } catch (Exception e) {
            log.error("Error getting admin statistics", e);
            // Возвращаем пустую статистику
            return new AdminStatsRsDto();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminUser> findByEmail(String email) {
        try {
            return adminUserRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Error finding admin by email", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public AdminRsDto getAdminByEmail(String email) {
        AdminUser adminUser = adminUserRepository.findByEmail(email).orElseThrow();
        return adminMapper.toDto(adminUser);
    }

    @Transactional
    public void updateLastLogin(String email) {
        try {
            adminUserRepository.findByEmail(email).ifPresent(admin -> {
                admin.setLastLoginAt(LocalDateTime.now());
                adminUserRepository.save(admin);
            });
        } catch (Exception e) {
            log.error("Error updating last login", e);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw ExceptionFactory.validationError("Пароль должен содержать минимум 8 символов");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw ExceptionFactory.validationError("Пароль должен содержать хотя бы одну заглавную букву");
        }

        if (!password.matches(".*[0-9].*")) {
            throw ExceptionFactory.validationError("Пароль должен содержать хотя бы одну цифру");
        }
    }
}