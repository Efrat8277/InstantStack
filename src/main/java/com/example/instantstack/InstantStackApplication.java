package com.example.instantstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InstantStackApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstantStackApplication.class, args);
    }

}
