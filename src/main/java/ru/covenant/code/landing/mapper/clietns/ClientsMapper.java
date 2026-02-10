package ru.covenant.code.landing.mapper.clietns;

import org.mapstruct.*;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsStatusRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientsMapper {

    // ========== Entity Creation Methods ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "courseType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    Clients toEntity(ClientsRqDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "courseType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    Clients toEntity(ClientsUpdateRqDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(ru.covenant.code.landing.entity.enumerated.Status.NEW)")
    @Mapping(target = "priority", expression = "java(ru.covenant.code.landing.entity.enumerated.Priority.MEDIUM)")
    @Mapping(target = "courseType", source = "courseTypeEnum")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    Clients toNewEntity(ClientsRqDto dto);

    // Метод для обновления статуса (ИСПРАВЛЕН: убрал третий параметр)
    @Mapping(target = "status", source = "statusDto.statusEnum")
    @Mapping(target = "processedBy", source = "statusDto.processedBy")
    @Mapping(target = "processedAt", expression = "java(setProcessedAt(statusDto))")
    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStatus(@MappingTarget Clients client, ClientsStatusRqDto statusDto);

    // Вспомогательный метод для conditional логики
    default OffsetDateTime setProcessedAt(ClientsStatusRqDto statusDto) {
        return (statusDto.getProcessedBy() != null && !statusDto.getProcessedBy().isEmpty())
                ? OffsetDateTime.now()
                : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(
            target = "courseType",
            source = "dto.courseTypeEnum",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "status",
            source = "dto.statusEnum",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "priority",
            source = "dto.priorityEnum",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "source",
            source = "dto.source",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    void updateEntity(@MappingTarget Clients client, ClientsUpdateRqDto dto);

    @AfterMapping
    default void setProcessed(ClientsUpdateRqDto dto, @MappingTarget Clients client) {
        if (dto != null && dto.getProcessedBy() != null && !dto.getProcessedBy().isEmpty()) {
            client.setProcessedBy(dto.getProcessedBy());
            client.setProcessedAt(OffsetDateTime.now());
        }
    }

    // ========== Response Mapping Methods ==========

    // Для ClientsCreateRsDto
    @Mapping(target = "courseType", source = "courseType", qualifiedByName = "courseTypeToString")
    ClientsCreateRsDto toCreateResponse(Clients client);

    // Для ClientsAdminRsDto
    @Mapping(target = "statusLabel", source = "status", qualifiedByName = "getStatusLabel")
    @Mapping(target = "priorityLabel", source = "priority", qualifiedByName = "getPriorityLabel")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "processedAt", source = "processedAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "formattedCreatedAt", source = "createdAt", qualifiedByName = "formatOffsetDateTime")
    @Mapping(target = "formattedUpdatedAt", source = "updatedAt", qualifiedByName = "formatOffsetDateTime")
    @Mapping(target = "formattedProcessedAt", source = "processedAt", qualifiedByName = "formatOffsetDateTime")
    ClientsAdminRsDto toAdminResponse(Clients client);

    List<ClientsAdminRsDto> toAdminResponseList(List<Clients> clients);

    // Метод для создания статистики
    @Named("createStatsDto")
    default ClientsStatsRsDto createStatsDto(
            long total, long newCount, long processedCount, long doneCount,
            long todayCount, long fullstackCount, long frontendCount,
            long backendCount, long highPriorityCount, long mediumPriorityCount,
            long lowPriorityCount) {
        ClientsStatsRsDto stats = new ClientsStatsRsDto();
        stats.setTotal(total);
        stats.setNewCount(newCount);
        stats.setProcessedCount(processedCount);
        stats.setDoneCount(doneCount);
        stats.setTodayCount(todayCount);
        stats.setFullstackCount(fullstackCount);
        stats.setFrontendCount(frontendCount);
        stats.setBackendCount(backendCount);
        stats.setHighPriorityCount(highPriorityCount);
        stats.setMediumPriorityCount(mediumPriorityCount);
        stats.setLowPriorityCount(lowPriorityCount);
        return stats;
    }

    // ========== Utility Methods ==========

    @Named("offsetDateTimeToString")
    default String offsetDateTimeToString(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }

    @Named("formatOffsetDateTime")
    default String formatOffsetDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }

    @Named("courseTypeToString")
    default String courseTypeToString(CourseType courseType) {
        if (courseType == null) {
            return null;
        }
        return courseType.name();
    }

    @Named("getStatusLabel")
    default String getStatusLabel(Status status) {
        if (status == null) {
            return null;
        }
        // Используем displayName из enum
        return status.getDisplayName();
    }

    @Named("getPriorityLabel")
    default String getPriorityLabel(Priority priority) {
        if (priority == null) {
            return null;
        }
        // Используем displayName из enum
        return priority.getDisplayName();
    }
}