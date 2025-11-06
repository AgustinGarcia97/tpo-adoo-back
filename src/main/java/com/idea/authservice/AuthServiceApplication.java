package com.idea.authservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        System.getenv().forEach((key, value) -> System.out.println(key + ": " + value));
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
