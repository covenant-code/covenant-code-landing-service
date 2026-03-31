package ru.covenant.code.landing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestDto {
    @NotBlank(message = "{client.name.notblank}")
    private String name;
    @Email(message = "{Email}")
    private String email;

}
