package ru.covenant.code.landing.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClientsRepositoryTest {

    @Autowired
    private ClientsRepository clientsRepository;

    private OffsetDateTime now;
    private OffsetDateTime threeDaysAgo;
    private OffsetDateTime twoDaysAgo;
    private OffsetDateTime oneDayAgo;
    private OffsetDateTime tomorrow;

    @BeforeEach
    void setUp() {
        clientsRepository.deleteAll();
        clientsRepository.flush();

        now = OffsetDateTime.now(ZoneOffset.UTC);
        threeDaysAgo = now.minusDays(3);
        twoDaysAgo = now.minusDays(2);
        oneDayAgo = now.minusDays(1);
        tomorrow = now.plusDays(1);
    }

    private List<Clients> createTestClients() {
        Clients client1 = Clients.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Хочу на Backend")
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .priority(Priority.HIGH)
                .source("Лендинг")
                .createdAt(threeDaysAgo)
                .updatedAt(threeDaysAgo)
                .build();

        Clients client2 = Clients.builder()
                .name("Петр Петров")
                .email("petr@example.com")
                .phone("+79167654321")
                .message("Интересует Frontend")
                .courseType(CourseType.FRONTEND)
                .status(Status.PROCESSED)
                .priority(Priority.MEDIUM)
                .source("Лендинг")
                .createdAt(twoDaysAgo)
                .updatedAt(twoDaysAgo)
                .processedAt(oneDayAgo)
                .processedBy("admin@example.com")
                .build();

        Clients client3 = Clients.builder()
                .name("Сергей Сергеев")
                .email("sergey@example.com")
                .phone("+79169876543")
                .message("Хочу на Fullstack")
                .courseType(CourseType.FULLSTACK)
                .status(Status.NEW)
                .priority(Priority.LOW)
                .source("Лендинг")
                .createdAt(oneDayAgo)
                .updatedAt(oneDayAgo)
                .build();

        Clients client4 = Clients.builder()
                .name("Анна Аннова")
                .email("anna@example.com")
                .phone("+79165554433")
                .message("Записалась на Backend")
                .courseType(CourseType.BACKEND)
                .status(Status.DONE)
                .priority(Priority.MEDIUM)
                .source("Лендинг")
                .createdAt(now)
                .updatedAt(now)
                .processedAt(now)
                .processedBy("admin@example.com")
                .build();

        List<Clients> clients = List.of(client1, client2, client3, client4);

        clientsRepository.saveAll(clients);
        clientsRepository.flush();

        return clients;
    }

    @Test
    @DisplayName("findByStatus с сортировкой по createdAt DESC")
    void findByStatus_ShouldFilterAndSort() {
        // Given
        createTestClients();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Clients> newClients = clientsRepository.findByStatus(Status.NEW, sort);

        assertThat(newClients)
                .isNotNull()
                .hasSize(2)
                .extracting(Clients::getStatus)
                .containsOnly(Status.NEW);

        assertThat(newClients.get(0).getCreatedAt())
                .isAfter(newClients.get(1).getCreatedAt());
        assertThat(newClients.get(0).getName()).isEqualTo("Сергей Сергеев");
        assertThat(newClients.get(1).getName()).isEqualTo("Иван Иванов");
    }

    @Test
    @DisplayName("findByStatus для всех значений Status enum")
    void findByStatus_ShouldWorkForAllStatusEnums() {
        createTestClients();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Clients> newClients = clientsRepository.findByStatus(Status.NEW, sort);
        assertThat(newClients).hasSize(2);
        assertThat(newClients).allMatch(c -> c.getStatus() == Status.NEW);

        List<Clients> processedClients = clientsRepository.findByStatus(Status.PROCESSED, sort);
        assertThat(processedClients).hasSize(1);
        assertThat(processedClients.get(0).getStatus()).isEqualTo(Status.PROCESSED);
        assertThat(processedClients.get(0).getName()).isEqualTo("Петр Петров");

        List<Clients> doneClients = clientsRepository.findByStatus(Status.DONE, sort);
        assertThat(doneClients).hasSize(1);
        assertThat(doneClients.get(0).getStatus()).isEqualTo(Status.DONE);
        assertThat(doneClients.get(0).getName()).isEqualTo("Анна Аннова");
    }

    @Test
    @DisplayName("countByStatus возвращает правильное количество")
    void countByStatus_ShouldReturnCorrectCount() {
        createTestClients();

        assertThat(clientsRepository.countByStatus(Status.NEW)).isEqualTo(2);
        assertThat(clientsRepository.countByStatus(Status.PROCESSED)).isEqualTo(1);
        assertThat(clientsRepository.countByStatus(Status.DONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("countByCreatedAtBetween с пустым периодом возвращает 0")
    void countByCreatedAtBetween_WithEmptyPeriod_ShouldReturnZero() {
        createTestClients();

        long count = clientsRepository.countByCreatedAtBetween(
                now.minusDays(100),
                now.minusDays(50)
        );
        assertThat(count).isZero();

        count = clientsRepository.countByCreatedAtBetween(
                now.plusDays(1),
                now.plusDays(10)
        );
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("countByCreatedAtBetween с обратным порядком дат")
    void countByCreatedAtBetween_WithReversedDates_ShouldReturnZero() {
        createTestClients();

        long count = clientsRepository.countByCreatedAtBetween(tomorrow, twoDaysAgo);
        assertThat(count).isZero();
    }
}