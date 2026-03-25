package ru.covenant.code.landing.ws.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.ws.dto.WebSocketMessageDto;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketPublisher webSocketPublisher;

    @BeforeEach
    void setUp() {
        webSocketPublisher = new WebSocketPublisher(messagingTemplate, null);
    }

    @Test
    void publishApplicationUpdated_ShouldSendCorrectMessage() {
        ClientsAdminRsDto application = new ClientsAdminRsDto();
        application.setId(UUID.randomUUID());
        application.setName("Иван Иванов");
        application.setEmail("ivan@example.com");
        application.setPhone("+79161234567");

        webSocketPublisher.publishApplicationUpdated(application);

        verify(messagingTemplate).convertAndSend(eq("/topic/applications"), any(WebSocketMessageDto.class));
    }

    @Test
    void publishStatsUpdated_ShouldSendCorrectMessage() {
        ClientsStatsRsDto stats = new ClientsStatsRsDto();
        stats.setTotal(1250L);
        stats.setNewCount(15L);
        stats.setProcessedCount(25L);
        stats.setDoneCount(1200L);

        webSocketPublisher.publishStatsUpdated(stats);

        verify(messagingTemplate).convertAndSend(eq("/topic/applications"), any(WebSocketMessageDto.class));
    }
}