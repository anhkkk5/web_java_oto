package com.carsales.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to Car Sales API! 🚗";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
