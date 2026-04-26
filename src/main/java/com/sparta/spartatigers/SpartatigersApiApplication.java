package com.sparta.spartatigers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class SpartatigersApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpartatigersApiApplication.class, args);
    }

}
