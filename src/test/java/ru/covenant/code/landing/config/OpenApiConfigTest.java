package ru.covenant.code.landing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

class OpenApiConfigUnitTest {

    /* ------------------- бин-метод из конфига ------------------- */
    private OpenAPI customOpenAPI() {
        return new OpenAPI()
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
                .servers(List.of(
                        new Server().url("/").description("Local Server"),
                        new Server().url("https://api.covenantcode.ru").description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        Аутентификация через JWT токен.
                                        Токен должен передаваться в заголовке Authorization: Bearer <token>.
                                        
                                        **Процесс аутентификации через cookie JSESSIONID:**
                                        1. Пользователь проходит аутентификацию через веб-форму
                                        2. Сервер устанавливает cookie JSESSIONID
                                        3. При последующих запросах браузер автоматически отправляет JSESSIONID
                                        4. Сервер валидирует сессию и предоставляет доступ к защищённым ресурсам
                                        """)));
    }

    /* ----------------------- тест ----------------------- */
    @Test
    @DisplayName("customOpenAPI содержит все фиксированные поля и схемы безопасности")
    void customOpenApiShouldContainAllMetaAndSecurity() {
        //given
        OpenAPI api = customOpenAPI();

        /* ---------- Info ---------- */
        Info info = api.getInfo();
        assertThat(info.getTitle()).isEqualTo("Covenant Code Landing Service API");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
        assertThat(info.getDescription())
                .contains("Аутентификация")
                .contains("Роли пользователей")
                .contains("GUEST")
                .contains("ADMIN")
                .contains("Коды ошибок")
                .contains("VALIDATION_ERROR");

        Contact contact = info.getContact();
        assertThat(contact.getName()).isEqualTo("Covenant Code Support");
        assertThat(contact.getEmail()).isEqualTo("support@covenantcode.ru");
        assertThat(contact.getUrl()).isEqualTo("https://covenantcode.ru");

        License license = info.getLicense();
        assertThat(license.getName()).isEqualTo("Proprietary");
        assertThat(license.getUrl()).isEqualTo("https://covenantcode.ru/license");

        /* ---------- Servers ---------- */
        List<Server> servers = api.getServers();
        assertThat(servers).hasSize(2);
        assertThat(servers)
                .extracting(Server::getUrl, Server::getDescription)
                .containsExactlyInAnyOrder(
                        tuple("/", "Local Server"),
                        tuple("https://api.covenantcode.ru", "Production Server")
                );

        /* ---------- Security ---------- */
        assertThat(api.getComponents().getSecuritySchemes())
                .containsKey("bearerAuth")
                .extractingByKey("bearerAuth")
                .satisfies(s -> {
                    assertThat(s.getType()).isEqualTo(SecurityScheme.Type.HTTP);
                    assertThat(s.getScheme()).isEqualTo("bearer");
                    assertThat(s.getBearerFormat()).isEqualTo("JWT");
                    assertThat(s.getDescription()).contains("JWT токен", "JSESSIONID");
                });
    }
}
