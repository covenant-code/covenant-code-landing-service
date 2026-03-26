package ru.covenant.code.landing.mapper;

import org.mapstruct.*;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import org.hibernate.validator.constraints.UUID;
import org.mapstruct.*;
import org.springframework.stereotype.Component;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.dto.client.response.LoginStatsRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(
        componentModel = "spring",  // ЭТО ВАЖНО! Делает маппер Spring bean'ом
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ClientsMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "processedAt", source = "processedAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusToLabel")
    @Mapping(target = "priorityLabel", source = "priority", qualifiedByName = "priorityToLabel")
    @Mapping(target = "formattedCreatedAt", source = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "formattedUpdatedAt", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "formattedProcessedAt", source = "processedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "source", constant = "Лендинг")
    ClientsAdminRsDto toAdminResponse(Clients clients);

    List<ClientsAdminRsDto> toAdminResponseList(List<Clients> clients);

    @Named("statusToLabel")
    default String statusToLabel(Status status) {
        return status != null ? status.getDisplayName() : null;
    }

    @Named("priorityToLabel")
    default String priorityToLabel(Priority priority) {
        return priority != null ? priority.getDisplayName() : null;
    }

    @Named("formatDateTime")
    default String formatDateTime(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    @Named("offsetDateTimeToString")
    default String offsetDateTimeToString(OffsetDateTime value) {
        return value != null ? value.toString() : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courseType", source = "courseType", qualifiedByName = "stringToCourseType", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    void updateEntity(@MappingTarget Clients clients, ClientsUpdateRqDto dto);

    @AfterMapping
    default void setProcessed(@MappingTarget Clients clients, ClientsUpdateRqDto dto){
        if(dto.getProcessedBy() != null && !dto.getProcessedBy().isBlank()){
            clients.setProcessedBy(dto.getProcessedBy());
            clients.setProcessedAt(OffsetDateTime.now());
        }
    }

    @Named("stringToCourseType")
    default CourseType stringToCourseType(String courseType) {
        if (courseType == null || courseType.isBlank()) {
            return null;
        }
        try {
            return CourseType.valueOf(courseType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("stringToStatus")
    default Status stringToStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("stringToPriority")
    default Priority stringToPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        try {
            return Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ==== Маппинг Request → Entity ====
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "priority", ignore = true),
            @Mapping(target = "source", ignore = true)
    })
    Clients toNewEntity(ClientsRqDto request);

    // ==== Маппинг LoginStats → ClientsStats ====
    @Mappings({
            @Mapping(target = "todayCount", source = "todayApplications"),  // todayApplications → todayCount
            @Mapping(target = "total", source = "totalApplications"),       // totalApplications → total
            @Mapping(target = "doneCount", source = "successfulApplications"), // successfulApplications → doneCount
            @Mapping(target = "newCount", constant = "0L"),                 // Если в LoginStatsRsDto нет newApplications
            @Mapping(target = "processedCount", constant = "0L"),           // Если в LoginStatsRsDto нет processedCount
            @Mapping(target = "fullstackCount", constant = "0L"),           // Если в LoginStatsRsDto нет fullstackCount
            @Mapping(target = "frontendCount", constant = "0L"),            // Если в LoginStatsRsDto нет frontendCount
            @Mapping(target = "backendCount", constant = "0L"),             // Если в LoginStatsRsDto нет backendCount
            @Mapping(target = "highPriorityCount", constant = "0L"),        // Если в LoginStatsRsDto нет highPriorityCount
            @Mapping(target = "mediumPriorityCount", constant = "0L"),      // Если в LoginStatsRsDto нет mediumPriorityCount
            @Mapping(target = "lowPriorityCount", constant = "0L")          // Если в LoginStatsRsDto нет lowPriorityCount
    })
    ClientsStatsRsDto toClientsStats(LoginStatsRsDto loginStats);

    // ==== Маппинг Entity → Create Response ====
    @Mappings({
            @Mapping(target = "result.id", source = "id"), // Маппим id из Clients в result.id
            @Mapping(target = "result.name", source = "name"),
            @Mapping(target = "result.email", source = "email"),
            @Mapping(target = "result.phone", source = "phone"),
            @Mapping(target = "result.courseType", source = "courseType"),
            @Mapping(target = "result.createdAt", source = "createdAt", qualifiedByName = "offsetToLocalDateTime"),
            @Mapping(target = "result.status", constant = "SUCCESS"),
            @Mapping(target = "message", constant = "Заявка успешно создана")
    })
    ClientsCreateRsDto toCreateResponse(Clients client);

    // --- Кастомный маппер для OffsetDateTime → LocalDateTime ---
    @Named("offsetToLocalDateTime")
    default LocalDateTime offsetToLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
