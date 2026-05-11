package com.example.splashstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.splashstore")
public class SplashStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplashStoreApplication.class, args);
    }

}
