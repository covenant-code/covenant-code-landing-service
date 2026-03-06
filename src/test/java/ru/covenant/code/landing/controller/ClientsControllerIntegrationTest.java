package ru.covenant.code.landing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.ws.service.WebSocketPublisher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ClientsRepository clientsRepository;

    @MockitoBean
    private WebSocketPublisher webSocketPublisher;

    @BeforeEach
    void setUp() {
        clientsRepository.deleteAll();
    }

    private ClientsRqDto validDto(String email) {
        return ClientsRqDto.builder()
                .name("Иван Петров")
                .email(email)
                .phone("+79161234567")
                .message("Интересуюсь курсом по бэкенду")
                .courseType("BACKEND")
                .build();
    }

    @Test
    void createApplication_validRequest_shouldReturn201() throws Exception {
        ClientsRqDto dto = validDto("ivan@example.com");

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(jsonPath("$.result.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.result.status").value("NEW"))
                .andExpect(jsonPath("$.result.message").isNotEmpty());
    }

    @Test
    void createApplication_invalidRequest_shouldReturn400() throws Exception {
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("И")
                .email("неправильный-email")
                .courseType("FRONTEND")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createApplication_duplicateEmail_shouldReturn409() throws Exception {
        ClientsRqDto dto = validDto("duplicate@example.com");

        // Первая заявка — успешно
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Вторая с тем же email — конфликт
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CLIENT_DUPLICATE"));
    }
}
