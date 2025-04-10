package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.controller", "com.example.service"})
public class CardLoginSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardLoginSystemApplication.class, args);
    }
} 