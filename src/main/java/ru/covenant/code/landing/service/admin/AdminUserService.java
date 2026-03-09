package ru.covenant.code.landing.service.admin;

import ru.covenant.code.landing.dto.admin.request.AdminLoginRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminLoginRsDto;

public interface AdminUserService {

    AdminLoginRsDto updateLastLogin(AdminLoginRqDto loginRequest);
}
