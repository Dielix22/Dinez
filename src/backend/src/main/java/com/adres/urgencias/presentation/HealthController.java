package com.adres.urgencias.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/helloworld")
    public Map<String, String> helloWorld() {
        return Map.of(
            "status", "success",
            "message", "Hello World from Dinez Backend!"
        );
    }
}