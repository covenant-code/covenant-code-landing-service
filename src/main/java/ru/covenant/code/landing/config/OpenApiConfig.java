package ru.covenant.code.landing.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;
import java.util.Arrays;

import ru.covenant.code.landing.entity.enumerated.AdminRole;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)

public class OpenApiConfig {
    private final String appName;

    public OpenApiConfig(@Value("${spring.application.name}") String appName) {
        this.appName = appName;

    }

    static {
        // Регистрация админ роли
        SpringDocUtils.getConfig();
        SpringDocUtils.getConfig().replaceWithSchema(
                AdminRole.class,
                new StringSchema()
                .addEnumItem(AdminRole.SUPER_ADMIN.name())
                .addEnumItem(AdminRole.ADMIN.name())
                .addEnumItem(AdminRole.MODERATOR.name())
                .addEnumItem(AdminRole.SUPPORT.name())
                .description("Роль администратора")
                .example(AdminRole.ADMIN.name())
        );

        //Типы курсов
        SpringDocUtils.getConfig().replaceWithSchema(
                CourseType.class,
                new StringSchema()
                .addEnumItem(CourseType.FULLSTACK.name())
                .addEnumItem(CourseType.FRONTEND.name())
                .addEnumItem(CourseType.BACKEND.name())
                .description("Тип курса")
                .example(CourseType.BACKEND.name())
        );

        //Статусы заявок
        SpringDocUtils.getConfig().replaceWithSchema(
                Status.class,
                new StringSchema()
                .addEnumItem(Status.NEW.name())
                .addEnumItem(Status.PROCESSED.name())
                .addEnumItem(Status.DONE.name())
                .description("Статус заявки")
                .example(Status.NEW.name())
        );

        //Приоритеты заявок
        SpringDocUtils.getConfig().replaceWithSchema(
                Priority.class,
                new StringSchema()
                .addEnumItem(Priority.HIGH.name())
                .addEnumItem(Priority.MEDIUM.name())
                .addEnumItem(Priority.LOW.name())
                .description("Приоритет заявки")
                .example(Priority.MEDIUM.name())
        );
    }

    //создание конфигурации OpenApi
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Настройка информации (Info)
                .info(new Info()
                        .title("Covenant Code Landing Service API")
                        .description("""
                                ### Аутентификация
                                
                                **Два типа аутентификации:**
                                - **Веб-форма**: Стандартная форма входа через веб-интерфейс
                                - **REST API**: Аутентификация через API endpoints с JWT токенами
                                
                                ### Роли пользователей
                                
                                **4 роли с описанием:**
                                1. **GUEST** - Пользователь без аутентификации, имеет доступ только к публичным ресурсам
                                2. **USER** - Зарегистрированный пользователь, имеет доступ к личному кабинету и базовым функциям
                                3. **MODERATOR** - Модератор, может управлять контентом и пользователями
                                4. **ADMIN** - Администратор, имеет полный доступ ко всем функциям системы
                                
                                ### Коды ошибок
                                
                                **Основные коды ошибок:**
                                - **VALIDATION_ERROR** - Ошибка валидации входных данных
                                - **NOT_FOUND** - Запрашиваемый ресурс не найден
                                - **UNAUTHORIZED** - Ошибка аутентификации
                                - **FORBIDDEN** - Недостаточно прав для выполнения операции
                                - **INTERNAL_SERVER_ERROR** - Внутренняя ошибка сервера
                                
                                ### Коды ответов HTTP
                                
                                **Основные HTTP статусы:**
                                - **200 OK** - Успешный запрос
                                - **201 Created** - Ресурс успешно создан
                                - **400 Bad Request** - Неверный запрос
                                - **401 Unauthorized** - Требуется аутентификация
                                - **403 Forbidden** - Доступ запрещен
                                - **404 Not Found** - Ресурс не найден
                                - **500 Internal Server Error** - Внутренняя ошибка сервера
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Covenant Code Support")
                                .email("support@covenantcode.ru")
                                .url("https://covenantcode.ru"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://covenantcode.ru/license")))
                //Настройка серверов (Servers)
                .servers(Arrays.asList(
                        new Server().url("/").description("Local Server"),
                        new Server().url("https://api.covenantcode.ru").description("Production Server")
                ))

                //Настройка безопасности (Security)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                                Аутентификация через JWT токен.
                                                Токен должен передаваться в заголовке Authorization: Bearer <token>.
                                                
                                                **Процесс аутентификации через cookie JSESSIONID:**
                                                1. Пользователь проходит аутентификацию через веб-форму
                                                2. Сервер устанавливает cookie JSESSIONID
                                                3. При последующих запросах браузер автоматически отправляет JSESSIONID
                                                4. Сервер валидирует сессию и предоставляет доступ к защищенным ресурсам
                                                """)));


    }
}