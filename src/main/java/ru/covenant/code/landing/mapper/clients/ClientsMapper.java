package ru.covenant.code.landing.mapper.clients;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.entity.Clients;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ClientsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "courseType", expression = "java(dto.getCourseTypeEnum())")
    @Mapping(target = "source", expression = "java(dto.getSource() != null ? dto.getSource() : \"Лендинг\")")
    Clients toNewEntity(ClientsRqDto dto);

    ClientsCreateRsDto toCreateResponse(Clients entity);

    @Mapping(target = "statusLabel",          expression = "java(entity.getStatus().getDisplayName())")
    @Mapping(target = "priorityLabel",        expression = "java(entity.getPriority().getDisplayName())")
    @Mapping(target = "createdAt",            expression = "java(entity.getCreatedAt()   != null ? entity.getCreatedAt().toString()   : null)")
    @Mapping(target = "updatedAt",            expression = "java(entity.getUpdatedAt()   != null ? entity.getUpdatedAt().toString()   : null)")
    @Mapping(target = "processedAt",          expression = "java(entity.getProcessedAt() != null ? entity.getProcessedAt().toString() : null)")
    @Mapping(target = "formattedCreatedAt",   expression = "java(formatDate(entity.getCreatedAt()))")
    @Mapping(target = "formattedUpdatedAt",   expression = "java(formatDate(entity.getUpdatedAt()))")
    @Mapping(target = "formattedProcessedAt", expression = "java(formatDate(entity.getProcessedAt()))")
    ClientsAdminRsDto toAdminResponse(Clients entity);

    default String formatDate(OffsetDateTime dt) {
        if (dt == null) return null;
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(dt);
    }
}
