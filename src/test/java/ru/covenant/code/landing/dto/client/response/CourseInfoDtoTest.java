package ru.covenant.code.landing.dto.client.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CourseInfoDtoTest {

    //Создали объект для преобразования объектов в json и обратно
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* Проверяем, что все геттеры возвращают правильные значения*/
    @Test
    public void testConstructorAndGetters() {
        // Создаем объект CourseInfoDto
        CourseInfoDto courseInfo = new CourseInfoDto("BACKEND", "Backend разработка",
                "Разработка серверной логики и API", "5 месяцев", "30 000 ₽");

        assertEquals("BACKEND", courseInfo.getCode());
        assertEquals("Backend разработка", courseInfo.getName());
        assertEquals("Разработка серверной логики и API", courseInfo.getDescription());
        assertEquals("5 месяцев", courseInfo.getDuration());
        assertEquals("30 000 ₽", courseInfo.getPrice());
    }

    /*Проверка аннотаций @Schema*/
    @Test
    public void testSchemaAnnotations() {
        try {
            Schema codeSchema = CourseInfoDto.class.getDeclaredField("code").getAnnotation(Schema.class);
            Schema nameSchema = CourseInfoDto.class.getDeclaredField("name").getAnnotation(Schema.class);
            Schema descriptionSchema = CourseInfoDto.class.getDeclaredField("description").getAnnotation(Schema.class);
            Schema durationSchema = CourseInfoDto.class.getDeclaredField("duration").getAnnotation(Schema.class);
            Schema priceSchema = CourseInfoDto.class.getDeclaredField("price").getAnnotation(Schema.class);

            assertNotNull(codeSchema);
            assertEquals("Код курса (значение Enum)", codeSchema.description());
            assertEquals("BACKEND", codeSchema.example());

            assertNotNull(nameSchema);
            assertEquals("Название курса (displayName)", nameSchema.description());
            assertEquals("Backend разработка", nameSchema.example());

            assertNotNull(descriptionSchema);
            assertEquals("Описание курса", descriptionSchema.description());
            assertEquals("Разработка серверной логики и API", descriptionSchema.example());

            assertNotNull(durationSchema);
            assertEquals("Длительность обучения", durationSchema.description());
            assertEquals("5 месяцев", durationSchema.example());

            assertNotNull(priceSchema);
            assertEquals("Стоимость курса", priceSchema.description());
            assertEquals("30 000 ₽", priceSchema.example());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Поле не найдено: " + e.getMessage(), e);
        }
    }

    /* Проверка, что Jackson корректно сериализует  */
    @Test
    public void testJsonSerialization() throws Exception {
        // Создаем объект CourseInfoDto
        CourseInfoDto courseInfo = new CourseInfoDto("BACKEND", "Backend разработка",
                "Разработка серверной логики и API", "5 месяцев", "30 000 ₽");

        // Сериализуем объект в JSON
        String jsonResult = objectMapper.writeValueAsString(courseInfo);

        // Создаем ожидаемый результат в виде Map
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("code", "BACKEND");
        expectedMap.put("name", "Backend разработка");
        expectedMap.put("description", "Разработка серверной логики и API");
        expectedMap.put("duration", "5 месяцев");
        expectedMap.put("price", "30 000 ₽");

        // Сериализуем ожидаемый результат в JSON
        String expectedJson = objectMapper.writeValueAsString(expectedMap);

        // Сравниваем два JSON-объекта как объекты
        Map<String, Object> actualMap = objectMapper.readValue(jsonResult, HashMap.class);
        Map<String, Object> expectedMapObject = objectMapper.readValue(expectedJson, HashMap.class);

        assertEquals(expectedMapObject, actualMap);
    }
}
