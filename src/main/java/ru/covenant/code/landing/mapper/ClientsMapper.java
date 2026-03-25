package ru.covenant.code.landing.mapper;

import org.mapstruct.*;
import org.springframework.stereotype.Component;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(
        componentModel = "spring",  // ЭТО ВАЖНО! Делает маппер Spring bean'ом
        unmappedTargetPolicy = ReportingPolicy.IGNORE
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
    @Mapping(target = "courseType", source = "courseType", qualifiedByName = "stringToCourseType")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")
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
}
