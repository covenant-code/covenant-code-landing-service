package ru.covenant.code.landing.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.util.CourseUtil;

import static org.junit.jupiter.api.Assertions.*;

class CourseUtilTest {

    private CourseUtil courseUtil;
//
//    @BeforeEach
//    public void setUp() {
//        courseUtil = new CourseUtil(); // Инициализация перед каждым тестом
//    }

    @Test
    public void testGetCourseDescription() {
        assertEquals("Полный цикл разработки веб-приложений: фронтенд и бэкенд",
                courseUtil.getCourseDescription(CourseType.FULLSTACK));
        assertEquals("Разработка пользовательских интерфейсов и клиентской части",
                courseUtil.getCourseDescription(CourseType.FRONTEND));
        assertEquals("Разработка серверной логики и API",
                courseUtil.getCourseDescription(CourseType.BACKEND));
    }

    @Test
    public void testGetCourseDuration() {
        assertEquals("6 месяцев",
                courseUtil.getCourseDuration(CourseType.FULLSTACK));
        assertEquals("4 месяца",
                courseUtil.getCourseDuration(CourseType.FRONTEND));
        assertEquals("5 месяцев",
                courseUtil.getCourseDuration(CourseType.BACKEND));
    }

    @Test
    public void testGetCoursePrice() {
        assertEquals("35 000 ₽",
                courseUtil.getCoursePrice(CourseType.FULLSTACK));
        assertEquals("25 000 ₽",
                courseUtil.getCoursePrice(CourseType.FRONTEND));
        assertEquals("30 000 ₽",
                courseUtil.getCoursePrice(CourseType.BACKEND));
    }
}
