package com.immobilier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@ComponentScan(basePackages = {"com.immobilier"})
@EnableMongoAuditing
public class ImmobilierApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImmobilierApiApplication.class, args);
        System.out.println("API Immobilier démarrée avec succès!");
    }
}
