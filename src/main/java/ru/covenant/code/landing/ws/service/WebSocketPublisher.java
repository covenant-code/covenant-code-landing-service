package ru.covenant.code.landing.ws.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.ws.dto.WebSocketMessageDto;

import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebSocketPublisher {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final LoginStatsService loginStatsService;

    public void publishApplicationCreated(ClientsAdminRsDto application) {
        try {
            log.info("Публикация создания заявки");

            var message = new WebSocketMessageDto("APPLICATION_CREATED", application);

            simpMessagingTemplate.convertAndSend("/topic/applications", message);

            log.debug("APPLICATION_CREATED успешно отправлено в /topic/applications");
        } catch (Exception e) {
            log.error("Ошибка при публикации события создания заявки ", e.getMessage(), e);
        }
    }

    public void publishApplicationUpdated(ClientsAdminRsDto application) {
        try {
            log.info("Публикация обновления заявки");

            var message = new WebSocketMessageDto("APPLICATION_UPDATED", application);

            simpMessagingTemplate.convertAndSend("/topic/applications", message);

            log.debug("APPLICATION_UPDATED успешно отправлено в /topic/applications");
        } catch (Exception e) {
            log.error("Ошибка при обновлении события создания заявки ", e.getMessage(), e);
        }
    }

    public void publishApplicationDeleted(String applicationId) {
        try {
            log.info("Публикация удаления заявки");

            var message = new WebSocketMessageDto("APPLICATION_DELETED", applicationId);

            simpMessagingTemplate.convertAndSend("/topic/applications", message);

            log.debug("APPLICATION_DELETED успешно отправлено в /topic/applications");
        } catch (Exception e) {
            log.error("Ошибка при удалении события создания заявки ", applicationId, e.getMessage(), e);
        }
    }

    public void publishStatsUpdated(ClientsStatsRsDto stats) {
        try {
            log.info("Публикация обновления статистики заявок");

            var message = new WebSocketMessageDto("STATS_UPDATED", stats);

            simpMessagingTemplate.convertAndSend("/topic/applications", message);

            log.debug("STATS_UPDATED успешно отправлено в /topic/applications");
        } catch (Exception e) {
            log.error("Ошибка при публикации статистики заявок ", e.getMessage(), e);
        }
    }

    public void publishLoginStatsUpdated(LoginStatsRsDto stats) {
        try {
            log.info("Публикация обновления статистики логинов");

            var message = new WebSocketMessageDto("LOGIN_STATS_UPDATED", stats);

            simpMessagingTemplate.convertAndSend("/topic/login-stats", message);

            log.debug("LOGIN_STATS_UPDATED успешно отправлено в /topic/login-stats");
        } catch (Exception e) {
            log.error("Ошибка публикация обновления статистики логинов", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void sendLoginStatsUpdate() {
        try {
            log.debug("Начало сбора и публикации статистики логинов");

            var stats = loginStatsService.getCurrentStats();
            var message = new WebSocketMessageDto();
            message.setType("LOGIN_STATS_UPDATED");
            message.setStats(stats);

            simpMessagingTemplate.convertAndSend("/topic/login-stats", message);

        } catch (Exception e) {
            log.error("Ошибка при публикации статистики логинов", e.getMessage(), e);
        }
    }
}
