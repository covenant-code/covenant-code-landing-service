package ru.covenant.code.landing.service.client.impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.exceptions.ExceptionFactory;
import ru.covenant.code.landing.exceptions.ValidationException;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.specification.ClientsSpecification;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientsServiceImpl implements ClientsService {

    private final ClientsRepository clientsRepository;
    private final ClientsSpecification clientsSpecification;
    private final ClientsMapper clientsMapper;

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
}
