package ru.covenant.code.landing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CovenantCodeLandingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CovenantCodeLandingServiceApplication.class, args);
    }

}
