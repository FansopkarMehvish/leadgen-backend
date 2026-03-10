package com.example.leadgen_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeadgenBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeadgenBackendApplication.class, args);
    }

}
