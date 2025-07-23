package com.handi.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
@Tag(name = "Redis Health", description = "Redis connection health check API")
public class RedisHealthController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    @Operation(summary = "Redis Health Check", description = "Check Redis connection status")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> response = new HashMap<>();

        try {
            String testKey = "health-check";
            String testValue = "test-" + System.currentTimeMillis();

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            if (testValue.equals(retrievedValue)) {
                response.put("status", "UP");
                response.put("message", "Redis connection is healthy");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("message", "Redis data integrity issue");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Redis connection failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @GetMapping("/ping")
    @Operation(summary = "Redis Ping", description = "Simple Redis ping test")
    public ResponseEntity<Map<String, Object>> pingRedis() {
        Map<String, Object> response = new HashMap<>();

        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            response.put("status", "UP");
            response.put("ping", pong);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}