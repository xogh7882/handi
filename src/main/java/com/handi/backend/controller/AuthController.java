package com.handi.backend.controller;

import com.handi.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth Controller" , description = "jwt 인증 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 갱신" , description = "Refresh Token을 사용하여 새로운 Access Token 발급")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = authService.refreshAccessToken(request, response);

            if (success) {
                result.put("success", true);
                result.put("message", "Access Token이 성공적으로 갱신되었습니다.");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "토큰 갱신에 실패했습니다. 다시 로그인해주세요.");
                return ResponseEntity.status(401).body(result);
            }

        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 모든 토큰을 무효화합니다.")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        Map<String, Object> result = new HashMap<>();

        try {
            // Request에서 userId 가져오기 (JWT 필터에서 설정된 값)
            Integer userId = (Integer) request.getAttribute("userId");

            if (userId != null) {
                authService.logout(userId, request, response);
                result.put("success", true);
                result.put("message", "성공적으로 로그아웃되었습니다.");
            } else {
                // userId가 없는 경우에도 쿠키는 삭제
                authService.clearAuthCookies(response);
                result.put("success", true);
                result.put("message", "로그아웃되었습니다.");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "로그아웃 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(result);
        }
    }



}
