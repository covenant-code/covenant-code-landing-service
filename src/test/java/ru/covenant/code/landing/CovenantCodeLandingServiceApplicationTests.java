package ru.covenant.code.landing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Используем тестовый профиль
class CovenantCodeLandingServiceApplicationTests {

    @Test
    void contextLoads() {
        // Пустой тест - просто проверяем, что контекст загружается
    }
}