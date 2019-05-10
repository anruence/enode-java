package com.enode.samples.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.enode", "com.enode.samples"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}