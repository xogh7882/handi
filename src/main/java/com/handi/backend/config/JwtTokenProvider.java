package com.handi.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration = 1000 * 60 * 30;  // 30분
    private final long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7; // 7일

    public JwtTokenProvider(@Value("${JWT_SECRET}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }


    // Access Token 생성
    public String generateAccessToken(String email, String name, String role, Integer userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(email)                   // subject : JWT 토큰이 누구것인지 확인
                .claim("type", "access")
                .claim("role", role)
                .claim("name", name)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 (사용자 정보 포함 X , 랜덤 문자열)
    public String generateRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        // 랜덤 ID 생성 (개인정보 대신 랜덤값 사용)
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String tokenId = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return Jwts.builder()
                .subject("refresh")           // 고정값 (개인정보 아님)
                .claim("type", "refresh")     // 토큰 타입
                .claim("userId", userId)
                .claim("refreshId", tokenId)        // JWT ID (랜덤값)
                .issuedAt(now)               // 발급 시간
                .expiration(expiration)       // 만료 시간
                .signWith(secretKey)         // 서명
                .compact();
    }

    // Refrsh Token에서 refreshId 추출
    public String getRefreshIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("refreshId", String.class);
        } catch (Exception e) {
            log.error("토큰에서 RefreshId 추출 실패: {}", e.getMessage());
            return null;
        }
    }


    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 토큰에서 역할 추출
    public String getNameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("name", String.class);
        } catch (Exception e) {
            log.error("토큰에서 이름 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 토큰에서 역할 추출
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("토큰에서 역할 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 토큰에서 사용자 ID 추출
    public Integer getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 토큰에서 type 추출
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("type", String.class);
        } catch (Exception e) {
            log.error("토큰에서 타입 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("잘못된 JWT 토큰: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 토큰: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT 클레임이 비어있음: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("JWT 토큰 검증 실패: {}", ex.getMessage());
        }
        return false;
    }

    // 너 RefreshToken 이니?
    public boolean isRefreshToken(String token) {
        String tokenType = getTokenType(token);
        return "refresh".equals(tokenType);
    }

    // Refresh Token 유효성 검사
    public boolean validateRefreshToken(String refreshToken) {
        try {
            if (!validateToken(refreshToken)) {  // 토큰 유효성 검증 통과 여부
                return false;
            }

            // Refresh Token 타입 확인
            if (!isRefreshToken(refreshToken)) {
                log.error("토큰이 Refresh Token이 아님");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 만료 시간 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 이미 만료됨
        } catch (Exception e) {
            log.error("토큰 만료 시간 확인 실패: {}", e.getMessage());
            return true;
        }
    }

}