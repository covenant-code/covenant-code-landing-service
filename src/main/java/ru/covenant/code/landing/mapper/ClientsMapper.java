package ru.covenant.code.landing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(
        componentModel = "spring",
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
}