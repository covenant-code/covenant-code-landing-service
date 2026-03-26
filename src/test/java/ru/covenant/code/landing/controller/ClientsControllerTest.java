package ru.covenant.code.landing.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import ru.covenant.code.landing.service.client.ClientsService;


@SpringBootTest
@AutoConfigureMockMvc // поднимает весь контекст приложения
@ActiveProfiles("test")
public class ClientsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientsService clientsService;

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetCoursesReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetCoursesReturnsCorrectDataStructure() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetCoursesReturnsCorrectNumberOfCourses() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(3));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetCoursesReturnsCorrectCourseDetailsFullstack() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].code").value("FULLSTACK"))
                .andExpect(jsonPath("$.result[0].name").value("Fullstack разработка"))
                .andExpect(jsonPath("$.result[0].description").value("Полный цикл разработки веб-приложений: фронтенд и бэкенд"))
                .andExpect(jsonPath("$.result[0].duration").value("6 месяцев"))
                .andExpect(jsonPath("$.result[0].price").value("35 000 ₽"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetCoursesReturnsCorrectCourseDetailsFrontend() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[1].code").value("FRONTEND"))
                .andExpect(jsonPath("$.result[1].name").value("Frontend разработка"))
                .andExpect(jsonPath("$.result[1].description").value("Разработка пользовательских интерфейсов и клиентской части"))
                .andExpect(jsonPath("$.result[1].duration").value("4 месяца"))
                .andExpect(jsonPath("$.result[1].price").value("25 000 ₽"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetCoursesReturnsCorrectCourseDetailsBackend() throws Exception {
        mockMvc.perform(get("/api/v1/clients/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[2].code").value("BACKEND"))
                .andExpect(jsonPath("$.result[2].name").value("Backend разработка"))
                .andExpect(jsonPath("$.result[2].description").value("Разработка серверной логики и API"))
                .andExpect(jsonPath("$.result[2].duration").value("5 месяцев"))
                .andExpect(jsonPath("$.result[2].price").value("30 000 ₽"));
    }
}



