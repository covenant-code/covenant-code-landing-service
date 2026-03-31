package ru.covenant.code.landing.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.UtilityClass;
import ru.covenant.code.landing.dto.client.response.CourseInfoDto;
import ru.covenant.code.landing.entity.enumerated.CourseType;

import java.util.List;

@UtilityClass //создает констр приватный все методы будут иметь модифик статик
public class CourseUtil {

    public String getCourseDescription(CourseType courseType) {
        return switch (courseType) {
            case FULLSTACK -> "Полный цикл разработки веб-приложений: фронтенд и бэкенд";
            case FRONTEND -> "Разработка пользовательских интерфейсов и клиентской части";
            case BACKEND -> "Разработка серверной логики и API";
        };
    }


    public String getCourseDuration(CourseType courseType) {
        return switch (courseType) {
            case FULLSTACK -> "6 месяцев";
            case FRONTEND -> "4 месяца";
            case BACKEND -> "5 месяцев";
        };
    }


    public String getCoursePrice(CourseType courseType) {
        return switch (courseType) {
            case FULLSTACK -> "35 000 ₽";
            case FRONTEND -> "25 000 ₽";
            case BACKEND -> "30 000 ₽";
        };
    }
}
