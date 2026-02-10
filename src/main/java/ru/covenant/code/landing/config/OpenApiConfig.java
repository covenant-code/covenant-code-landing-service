package ru.covenant.code.landing.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.covenant.code.landing.entity.enumerated.AdminRole;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.entity.enumerated.Priority;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    static {
        // Регистрация кастомных схем для Enum

        // Роли администраторов
        SpringDocUtils.getConfig().replaceWithSchema(AdminRole.class,
                new StringSchema()
                        .addEnumItem(AdminRole.SUPER_ADMIN.name())
                        .addEnumItem(AdminRole.ADMIN.name())
                        .addEnumItem(AdminRole.MODERATOR.name())
                        .addEnumItem(AdminRole.SUPPORT.name())
                        .description("Роль администратора")
                        .example(AdminRole.ADMIN.name()));

        // Типы курсов
        SpringDocUtils.getConfig().replaceWithSchema(CourseType.class,
                new StringSchema()
                        .addEnumItem(CourseType.FRONTEND.name())
                        .addEnumItem(CourseType.FULLSTACK.name())
                        .addEnumItem(CourseType.BACKEND.name())
                        .description("Тип курса")
                        .example(CourseType.BACKEND.name()));

        // Статусы заявок
        SpringDocUtils.getConfig().replaceWithSchema(Status.class,
                new StringSchema()
                        .addEnumItem(Status.NEW.name())
                        .addEnumItem(Status.PROCESSED.name())
                        .addEnumItem(Status.DONE.name())
                        .description("Статус заявки")
                        .example(Status.NEW.name()));

        // Приоритеты заявок
        SpringDocUtils.getConfig().replaceWithSchema(Priority.class,
                new StringSchema()
                        .addEnumItem(Priority.HIGH.name())
                        .addEnumItem(Priority.MEDIUM.name())
                        .addEnumItem(Priority.LOW.name())
                        .description("Приоритет заявки")
                        .example(Priority.MEDIUM.name()));
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Covenant Code Landing Service API")
                        .description("""
                    REST API сервиса лендингов Covenant Code.
                    
                    ## Аутентификация
                    Система использует **два типа аутентификации**:
                    
                    ### 1. Веб-интерфейс (форма логина):
                    - URL: /login (GET) - страница входа
                    - URL: /login (POST) - обработка формы (CSRF требуется)
                    
                    ### 2. REST API (JSON):
                    - URL: /api/admin/login (POST) - аутентификация через API
                    - CSRF: отключен для этого endpoint
                    - Возвращает: JSON с результатом
                    
                    ### Для тестирования через Swagger:
                    1. Выполните POST /api/admin/login с email и паролем
                    2. Swagger сохранит cookie JSESSIONID
                    3. Все последующие запросы будут аутентифицированы
                    
                    ### Учетные данные для dev среды:
                    - Email: admin@covenantcode.ru
                    - Пароль: admin123
                    
                    ## Роли пользователей
                    - **SUPER_ADMIN**: Полный доступ ко всем функциям
                    - **ADMIN**: Управление клиентами и базовые настройки
                    - **MODERATOR**: Модерация заявок клиентов
                    - **SUPPORT**: Просмотр информации о клиентах
                    
                    ## Коды ошибок
                    - **VALIDATION_ERROR**: Ошибка валидации входных данных
                    - **NOT_FOUND**: Ресурс не найден
                    - **UNAUTHORIZED**: Требуется аутентификация
                    - **FORBIDDEN**: Недостаточно прав
                    - **INTERNAL_ERROR**: Внутренняя ошибка сервера
                    
                    ## Коды ответов
                    - 200: Успешный запрос
                    - 201: Ресурс создан
                    - 400: Некорректный запрос
                    - 401: Неавторизован
                    - 403: Нет доступа
                    - 404: Ресурс не найден
                    - 500: Внутренняя ошибка сервера
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Covenant Code Support")
                                .email("support@covenantcode.ru")
                                .url("https://covenantcode.ru"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://covenantcode.ru/license")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Local Server"),
                        new Server()
                                .url("https://api.covenantcode.ru")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                            Для аутентификации используйте:
                            1. Авторизуйтесь через POST /api/admin/login
                            2. Установится cookie JSESSIONID
                            3. Все последующие запросы автоматически будут аутентифицированы
                            """)
                        ));
    }
}