package ru.covenant.code.landing.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.repository.ClientsRepository;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ClientsRepository clientRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing test data...");

        if (clientRepository.count() == 0) {
            // Добавляем тестовые заявки
            Clients client1 = Clients.builder()
                    .name("Иван Иванов")
                    .email("ivan@test.ru")
                    .phone("+79999999999")
                    .courseType(CourseType.FULLSTACK)
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .source("Лендинг")
                    .createdAt(OffsetDateTime.now().minusDays(1))
                    .build();

            Clients client2 = Clients.builder()
                    .name("Петр Петров")
                    .email("petr@test.ru")
                    .courseType(CourseType.BACKEND)
                    .status(Status.NEW)
                    .priority(Priority.HIGH)
                    .source("Социальные сети")
                    .createdAt(OffsetDateTime.now())
                    .build();

            clientRepository.save(client1);
            clientRepository.save(client2);

            log.info("Added {} test clients", clientRepository.count());
        }
    }
}
