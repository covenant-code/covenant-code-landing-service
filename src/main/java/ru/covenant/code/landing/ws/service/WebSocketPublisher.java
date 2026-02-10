package ru.covenant.code.landing.ws.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.dto.client.response.LoginStatsRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.service.client.LoginStatsService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;
    private final LoginStatsService loginStatsService;

    public void publishApplicationCreated(ClientsAdminRsDto application) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "APPLICATION_CREATED");
        message.put("application", application);  // Ключ "application", не "payload"

        messagingTemplate.convertAndSend("/topic/applications", Optional.of(message));
        System.out.println("📡 WS: Published APPLICATION_CREATED for: " + application.getId());
    }

    public void publishApplicationUpdated(ClientsAdminRsDto application) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "APPLICATION_UPDATED");
        message.put("application", application);

        messagingTemplate.convertAndSend("/topic/applications", Optional.of(message));
        System.out.println("📡 WS: Published APPLICATION_UPDATED for: " + application.getId());
    }

    public void publishApplicationDeleted(String applicationId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "APPLICATION_DELETED");
        message.put("applicationId", applicationId);

        messagingTemplate.convertAndSend("/topic/applications", Optional.of(message));
        System.out.println("📡 WS: Published APPLICATION_DELETED for: " + applicationId);
    }

    public void publishStatsUpdated(ClientsStatsRsDto stats) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATS_UPDATED");
        message.put("stats", stats);

        messagingTemplate.convertAndSend("/topic/applications", Optional.of(message));
        System.out.println("📡 WS: Published STATS_UPDATED");
    }

    // Публикация обновления статистики для страницы логина
    public void publishLoginStatsUpdated(LoginStatsRsDto stats) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOGIN_STATS_UPDATED");
        message.put("stats", stats);

        messagingTemplate.convertAndSend("/topic/login-stats", Optional.of(message));
        System.out.println("📡 WS: Published LOGIN_STATS_UPDATED");
    }

    // Автоматическое обновление каждые 30 секунд
    @Scheduled(fixedRate = 30000)
    public void sendLoginStatsUpdate() {
        try {
            LoginStatsRsDto stats = loginStatsService.getLoginPageStats();
            publishLoginStatsUpdated(stats);
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении статистики: " + e.getMessage());
        }
    }
}