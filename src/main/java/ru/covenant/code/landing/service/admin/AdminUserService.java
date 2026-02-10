package ru.covenant.code.landing.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.covenant.code.landing.dto.admin.request.AdminCreateRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminFilterRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminPasswordRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminUpdateRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminRsDto;
import ru.covenant.code.landing.dto.admin.response.AdminStatsRsDto;
import ru.covenant.code.landing.entity.AdminUser;

import java.util.Optional;
import java.util.UUID;

public interface AdminUserService {

    Page<AdminRsDto> getAllAdmins(AdminFilterRqDto filter, Pageable pageable);

    AdminRsDto getAdminById(UUID id);

    AdminRsDto createAdmin(AdminCreateRqDto createDto);

    AdminRsDto updateAdmin(UUID id, AdminUpdateRqDto updateDto);

    void deleteAdmin(UUID id);

    void changePassword(UUID id, AdminPasswordRqDto passwordDto);

    AdminRsDto toggleAdminStatus(UUID id);

    AdminStatsRsDto getAdminStats();

    Optional<AdminUser> findByEmail(String email);

    AdminRsDto getAdminByEmail(String email);
}