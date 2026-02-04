package ru.covenant.code.landing.service.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.dto.client.response.LoginStatsRsDto;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.LoginStatsService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginStatsServiceImpl implements LoginStatsService {

    private final ClientsRepository clientRepository;

    @Override
    public LoginStatsRsDto getLoginPageStats() {
        log.info("Getting login page statistics...");

        LoginStatsRsDto stats = new LoginStatsRsDto();

        try {
            // Количество новых заявок за сегодня
            LocalDate today = LocalDate.now();
            OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endOfDay = today.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

            long todayApplications = clientRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            stats.setTodayApplications(todayApplications);
            log.info("Today applications: {}", todayApplications);

            // Общее количество заявок
            long totalApplications = clientRepository.count();
            stats.setTotalApplications(totalApplications);
            log.info("Total applications: {}", totalApplications);

            // Количество завершенных заявок (DONE статус) - используем enum!
            long successfulApplications = clientRepository.countByStatus(Status.DONE);
            stats.setSuccessfulApplications(successfulApplications);
            log.info("Successful applications: {}", successfulApplications);

            // Процент успешных обращений
            int successRate = 0;
            if (totalApplications > 0) {
                successRate = (int) ((successfulApplications * 100) / totalApplications);
            }
            stats.setSuccessRate(successRate);
            log.info("Success rate: {}%", successRate);

        } catch (Exception e) {
            log.error("Error getting login stats: {}", e.getMessage(), e);
            throw e;
        }

        return stats;
    }
}