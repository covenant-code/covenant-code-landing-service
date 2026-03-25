package ru.covenant.code.landing.service.client.impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.exceptions.BusinessException;
import ru.covenant.code.landing.exceptions.ExceptionFactory;
import ru.covenant.code.landing.exceptions.ValidationException;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.specification.ClientsSpecification;
import ru.covenant.code.landing.ws.service.WebSocketPublisher;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientsServiceImpl implements ClientsService {

    private final ClientsRepository clientsRepository;
    private final ClientsSpecification clientsSpecification;
    private final ClientsMapper clientsMapper;
    private final WebSocketPublisher publisher;

    @Override
    @Transactional(readOnly = true)
    public List<ClientsAdminRsDto> getAllClients(ClientsFilterRqDto filter) {
        log.info("Запрос списка клиентов с фильтром: startDate={}, endDate={}, statuses={}, priorities={}, courseTypes={}, searchQuery={}",
                filter.getStartDate(), filter.getEndDate(), filter.getStatuses(),
                filter.getPriorities(), filter.getCourseTypes(), filter.getSearchQuery());

        try {
            var specification = clientsSpecification.withFilter(filter);
            var sort = Sort.by(Sort.Direction.DESC, "createdAt");

            var clients = clientsRepository.findAll(specification, sort);
            log.debug("Найдено {} клиентов", clients.size());

            return clientsMapper.toAdminResponseList(clients);

        } catch (Exception e) {
            log.error("Ошибка при получении списка клиентов: {}", e.getMessage(), e);
            throw ExceptionFactory.internalError("Ошибка при получении списка клиентов");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientsAdminRsDto> getClientsByStatus(String status) {
        log.info("Запрос на получение клиентов со статусом: {}", status);

        try {
            Status statusEnum = validateAndParseStatus(status);
            log.debug("Статус успешно преобразован в Enum: {}", statusEnum);

            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

            List<Clients> clients = clientsRepository.findByStatus(statusEnum, sort);
            log.debug("Найдено {} клиентов со статусом {}", clients.size(), statusEnum);

            return clientsMapper.toAdminResponseList(clients);

        } catch (ValidationException e) {
            log.warn("Ошибка валидации статуса: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Передан некорректный статус: {}", status);
            throw new ValidationException("Некорректный статус: " + status);
        } catch (Exception e) {
            log.error("Ошибка при получении клиентов по статусу: {}", status, e);
            throw new RuntimeException("Внутренняя ошибка сервера при получении клиентов", e);
        }
    }

    private Status validateAndParseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Статус не может быть пустым");
        }

        try {
            return Status.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Некорректный статус: " + status);
        }
    }

    @Override
    public ClientsAdminRsDto updateClient(UUID id, ClientsUpdateRqDto dto) {
        log.info("Обновление клиента");

        try {
            Clients client = clientsRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.clientNotFound(id));

            validateClientUpdate(dto);

            clientsMapper.updateEntity(client, dto);

            Clients updateClients = clientsRepository.save(client);

            ClientsAdminRsDto response = clientsMapper.toAdminResponse(updateClients);

            try {
                publisher.publishApplicationUpdated(response);

                ClientsStatsRsDto stats = getStats();
                publisher.publishStatsUpdated(stats);

                log.debug("Отправлены WebSocket уведомления для клиента {}", id);
            } catch (Exception e) {
                log.warn("Ошибка при отправке WebSocket уведомлений: {}", e.getMessage());
            }
            return response;
        } catch (BusinessException e) {
            log.warn("Бизнес-ошибка при обновлении клиента");
            throw e;
        } catch (Exception e) {
            log.error("Ошибка БД при обновлении клиента");
            throw ExceptionFactory.persistenceError("Clients", "обновление", e);        }


    }

    private void validateClientUpdate(ClientsUpdateRqDto dto) {

        if (dto.getEmail() != null && !dto.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать символ '@'");
        }

        if (dto.getPhone() != null && !dto.getPhone().matches("^\\+7[0-9]{10}$")) {
            throw new ValidationException("Телефон должен быть в формате +7XXXXXXXXXX");
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                Status.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Некорректный статус: " + dto.getStatus());
            }
        }

        if (dto.getPriority() != null && !dto.getPriority().isBlank()) {
            try {
                Priority.valueOf(dto.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Некорректный приоритет: " + dto.getPriority());
            }
        }

        if (dto.getCourseType() != null && !dto.getCourseType().isBlank()) {
            try {
                CourseType.valueOf(dto.getCourseType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Некорректный тип курса: " + dto.getCourseType());
            }
        }
    }

    private ClientsStatsRsDto getStats() {
        ClientsStatsRsDto stats = new ClientsStatsRsDto();

        try {
            stats.setTotal(clientsRepository.count());

            stats.setNewCount(clientsRepository.countByStatus(Status.NEW));
            stats.setProcessedCount(clientsRepository.countByStatus(Status.PROCESSED));
            stats.setDoneCount(clientsRepository.countByStatus(Status.DONE));

            OffsetDateTime startOfDay = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
            stats.setTodayCount(clientsRepository.countByCreatedAtBetween(startOfDay, endOfDay));

            stats.setFullstackCount(0L);
            stats.setFrontendCount(0L);
            stats.setBackendCount(0L);
            stats.setHighPriorityCount(0L);
            stats.setMediumPriorityCount(0L);
            stats.setLowPriorityCount(0L);

            log.debug("Статистика получена: total={}, new={}, processed={}, done={}, today={}",
                    stats.getTotal(), stats.getNewCount(), stats.getProcessedCount(),
                    stats.getDoneCount(), stats.getTodayCount());

        } catch (Exception e) {
            log.error("Ошибка при получении статистики", e);
            stats.setTotal(0);
            stats.setNewCount(0);
            stats.setProcessedCount(0);
            stats.setDoneCount(0);
            stats.setTodayCount(0);
            stats.setFullstackCount(0);
            stats.setFrontendCount(0);
            stats.setBackendCount(0);
            stats.setHighPriorityCount(0);
            stats.setMediumPriorityCount(0);
            stats.setLowPriorityCount(0);
        }

        return stats;
    }
}
