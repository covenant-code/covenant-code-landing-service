package ru.covenant.code.landing.mapper.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.covenant.code.landing.dto.admin.response.AdminRsDto;
import ru.covenant.code.landing.entity.AdminUser;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(source = "active", target = "active")
    @Mapping(source = "lastLoginAt", target = "lastLoginAt")
    @Mapping(source = "createdAt", target = "createdAt")
    AdminRsDto toDto(AdminUser adminUser);
}
