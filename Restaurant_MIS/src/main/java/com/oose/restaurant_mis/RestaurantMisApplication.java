package com.oose.restaurant_mis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RestaurantMisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantMisApplication.class, args);
    }

}
