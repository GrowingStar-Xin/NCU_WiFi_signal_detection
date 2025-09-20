package com.ncu.trackplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ncu.trackplatform.repository")
public class TrackPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackPlatformApplication.class, args);
    }

}