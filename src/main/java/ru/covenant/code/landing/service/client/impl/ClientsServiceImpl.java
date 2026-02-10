package ru.covenant.code.landing.service.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
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
import ru.covenant.code.landing.exceptions.*;
import ru.covenant.code.landing.mapper.clietns.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.specification.ClientsSpecification;
import ru.covenant.code.landing.ws.service.WebSocketPublisher;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientsServiceImpl implements ClientsService {

    private final ClientsRepository clientsRepository;
    private final ClientsMapper clientsMapper;
    private final WebSocketPublisher webSocketPublisher;

    @Override
    @Transactional
    public ClientsCreateRsDto create(ClientsRqDto dto) {
        try {
            // Проверка дубликатов (опционально)
            if (clientsRepository.existsByEmail(dto.getEmail())) {
                throw ExceptionFactory.clientDuplicate(dto.getEmail(), dto.getPhone());
            }

            Clients client = clientsMapper.toNewEntity(dto);
            Clients savedClient = clientsRepository.save(client);

            log.info("Created new application: {} for course: {}", savedClient.getId(), savedClient.getCourseType());

            ClientsCreateRsDto response = clientsMapper.toCreateResponse(savedClient);
            webSocketPublisher.publishApplicationCreated(clientsMapper.toAdminResponse(savedClient));
            webSocketPublisher.publishStatsUpdated(getStats());

            return response;
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw PersistenceException.save("клиент", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientsAdminRsDto> getAllClients(ClientsFilterRqDto filter) {
        try {
            Specification<Clients> spec = ClientsSpecification.withFilter(filter);
            List<Clients> clients = clientsRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
            return clientsMapper.toAdminResponseList(clients);
        } catch (Exception e) {
            throw ExceptionFactory.internalError("Ошибка при получении списка клиентов");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClientsAdminRsDto getClientById(UUID id) {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> ExceptionFactory.clientNotFound(id));
        return clientsMapper.toAdminResponse(client);
    }

    @Override
    @Transactional
    public ClientsAdminRsDto updateStatus(UUID id, ClientsStatusRqDto statusDto) {
        try {
            Clients client = clientsRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.clientNotFound(id));

            // Валидация изменения статуса
            validateStatusChange(client.getStatus().name(), statusDto.getStatus());

            clientsMapper.updateStatus(client, statusDto);

            Clients savedClient = clientsRepository.save(client);
            log.info("Updated status for client with id: {}, new status: {}", id, statusDto.getStatus());

            return clientsMapper.toAdminResponse(savedClient);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.update("клиент", e);
        }
    }

    private void validateStatusChange(String currentStatus, String newStatus) {
        // Логика валидации перехода статусов
        if ("DONE".equals(currentStatus) && !"DONE".equals(newStatus)) {
            throw ExceptionFactory.invalidClientStatus(currentStatus, newStatus);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        try {
            if (!clientsRepository.existsById(id)) {
                throw ExceptionFactory.clientNotFound(id);
            }

            clientsRepository.deleteById(id);
            log.info("Deleted application: {}", id);

            webSocketPublisher.publishApplicationDeleted(id.toString());
            webSocketPublisher.publishStatsUpdated(getStats());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.delete("клиент", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientsAdminRsDto> getClientsByStatus(String status) {
        try {
            Status statusEnum = Status.valueOf(status.toUpperCase());
            List<Clients> clients = clientsRepository.findByStatus(statusEnum, Sort.by(Sort.Direction.DESC, "createdAt"));
            return clientsMapper.toAdminResponseList(clients);
        } catch (IllegalArgumentException e) {
            throw ExceptionFactory.validationError("Некорректный статус: " + status);
        } catch (Exception e) {
            throw ExceptionFactory.internalError("Ошибка при получении клиентов по статусу");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClientsStatsRsDto getStats() {
        try {
            OffsetDateTime startOfDayOffset = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

            return clientsMapper.createStatsDto(
                    clientsRepository.count(),
                    clientsRepository.countByStatus(Status.NEW),
                    clientsRepository.countByStatus(Status.PROCESSED),
                    clientsRepository.countByStatus(Status.DONE),
                    clientsRepository.countByCreatedAtAfter(startOfDayOffset),
                    clientsRepository.countByCourseType(CourseType.FULLSTACK),
                    clientsRepository.countByCourseType(CourseType.FRONTEND),
                    clientsRepository.countByCourseType(CourseType.BACKEND),
                    clientsRepository.countByPriority(Priority.HIGH),
                    clientsRepository.countByPriority(Priority.MEDIUM),
                    clientsRepository.countByPriority(Priority.LOW)
            );
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            // Возвращаем пустую статистику вместо исключения для фронтенда
            return clientsMapper.createStatsDto(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    @Transactional
    public ClientsAdminRsDto updateClient(UUID id, ClientsUpdateRqDto dto) {
        try {
            Clients client = clientsRepository.findById(id)
                    .orElseThrow(() -> ExceptionFactory.clientNotFound(id));

            // Валидация данных перед обновлением
            validateClientUpdate(dto);

            clientsMapper.updateEntity(client, dto);

            Clients updatedClient = clientsRepository.save(client);
            log.info("Updated application {} by {}", id, dto.getProcessedBy());

            ClientsAdminRsDto responseDto = clientsMapper.toAdminResponse(updatedClient);
            webSocketPublisher.publishApplicationUpdated(responseDto);
            webSocketPublisher.publishStatsUpdated(getStats());

            return responseDto;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw PersistenceException.update("клиент", e);
        }
    }

    private void validateClientUpdate(ClientsUpdateRqDto dto) {
        // Можно добавить бизнес-логику валидации
        if (dto.getEmail() != null && !dto.getEmail().contains("@")) {
            throw ExceptionFactory.validationError("Некорректный email");
        }
    }
}