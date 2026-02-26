package ru.covenant.code.landing.view.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/index"})
    public String landingPage() {
        return "index"; // Без .html!
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Без .html!
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPage() {
        return "admin"; // Без .html!
    }
}