package com.foodrescue.pickup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PickupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickupServiceApplication.class, args);
    }
}
