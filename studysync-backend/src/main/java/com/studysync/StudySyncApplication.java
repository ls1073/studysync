package com.studysync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudySyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudySyncApplication.class, args);
    }
}
