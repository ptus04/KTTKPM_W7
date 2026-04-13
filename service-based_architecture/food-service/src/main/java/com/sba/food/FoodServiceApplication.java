package com.sba.food;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sba")
public class FoodServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodServiceApplication.class, args);
    }
}
