package ru.covenant.code.landing.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.covenant.code.landing.dto.response.ClientsAdminRsDto;
import ru.covenant.code.landing.service.ClientsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
public class AdminController {

    private final ClientsService clientsService;

    @GetMapping
    public List<ClientsAdminRsDto> getAllClients() {
        return clientsService.getAllAdminClients();
    }
}
