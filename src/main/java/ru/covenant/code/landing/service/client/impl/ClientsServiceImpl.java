package ru.covenant.code.landing.service.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.exceptions.BusinessException;
import ru.covenant.code.landing.exceptions.ExceptionFactory;
import ru.covenant.code.landing.mapper.clients.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.ws.service.WebSocketPublisher;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

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
            if (clientsRepository.existsByEmail(dto.getEmail())) {
                throw ExceptionFactory.clientDuplicate(dto.getEmail(), dto.getPhone());
            }

            Clients entity = clientsMapper.toNewEntity(dto);
            entity = clientsRepository.save(entity);
            log.info("Создана новая заявка: id={}, email={}", entity.getId(), entity.getEmail());

            ClientsCreateRsDto response = clientsMapper.toCreateResponse(entity);

            webSocketPublisher.publishApplicationCreated(clientsMapper.toAdminResponse(entity));
            webSocketPublisher.publishStatsUpdated(buildStats());

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw ExceptionFactory.persistenceError("Clients", "создание", e);
        }
    }

    private ClientsStatsRsDto buildStats() {
        ClientsStatsRsDto stats = new ClientsStatsRsDto();
        stats.setTotal(clientsRepository.count());
        stats.setNewCount(clientsRepository.countByStatus(Status.NEW));
        stats.setProcessedCount(clientsRepository.countByStatus(Status.PROCESSED));
        stats.setDoneCount(clientsRepository.countByStatus(Status.DONE));

        OffsetDateTime startOfDay = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
        stats.setTodayCount(clientsRepository.countByCreatedAtBetween(startOfDay, startOfDay.plusDays(1)));

        stats.setFullstackCount(clientsRepository.countByCourseType(CourseType.FULLSTACK));
        stats.setFrontendCount(clientsRepository.countByCourseType(CourseType.FRONTEND));
        stats.setBackendCount(clientsRepository.countByCourseType(CourseType.BACKEND));

        stats.setHighPriorityCount(clientsRepository.countByPriority(Priority.HIGH));
        stats.setMediumPriorityCount(clientsRepository.countByPriority(Priority.MEDIUM));
        stats.setLowPriorityCount(clientsRepository.countByPriority(Priority.LOW));

        return stats;
    }
}
