package com.example.mypixel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyPixelApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyPixelApplication.class, args);
    }
}
