package com.example.retail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
public class RetailApplication {

    public static void main(String[] args) {
        // Set SecurityContextHolder strategy to MODE_INHERITABLETHREADLOCAL
        //SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        // Get and print the application's process ID (PID)
        System.out.println("Application PID: " + ProcessHandle.current().pid());

        SpringApplication.run(RetailApplication.class, args);
    }

}