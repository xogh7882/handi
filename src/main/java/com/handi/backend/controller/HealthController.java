package com.handi.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "헬스체크 API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "헬스체크", description = "애플리케이션 상태를 확인합니다.")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Backend application is running successfully");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/info")
    @Operation(summary = "애플리케이션 정보", description = "애플리케이션 기본 정보를 반환합니다.")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Backend API");
        response.put("version", "1.0.0");
        response.put("description", "Spring Boot Backend Application");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
} 