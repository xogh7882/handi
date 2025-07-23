package com.handi.backend.service;

import com.handi.backend.util.CookieUtil;
import com.handi.backend.util.JwtTokenProvider;
import com.handi.backend.entity.Users;
import com.handi.backend.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsersRepository usersRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CookieUtil cookieUtil;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7; // 7일


    // Access Token 이 없을 때, 쿠키에서 Refresh Token 가져와서 Access Token 발급하기
    public Map<String, Object> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = cookieUtil.getCookieValue(request, "accessToken")
                .orElseThrow(() -> new IllegalArgumentException("accessToken 쿠키에 없음"));

        // 2. Refresh Token 유효성 검증
        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰");
        }

        // 3. Refresh Token 에서 사용자 ID 추출
        String refreshId = jwtTokenProvider.getRefreshIdFromToken(refreshToken);
        Integer userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if(refreshId == null) {
            throw new IllegalArgumentException("Refresh Token에서 사용자 ID 없음");
        }

        // 4. Redis에서 해당 사용자의 Refresh Token 유효성 확인
        String storedTokenKey = REFRESH_TOKEN_PREFIX + userId + ":" +  refreshId;
        Boolean tokenExists = redisTemplate.hasKey(storedTokenKey);

        if(!Boolean.TRUE.equals(tokenExists)) {
            throw new IllegalArgumentException("만료된 refresh token");
        }

        // 5. 사용자 정보 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 6. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getName(),
                user.getId()
        );

        // 7. 새로운 Access Token 쿠키에 저장
        cookieUtil.createAccessTokenCookie(response, newAccessToken);

        // 8. 응답 데이터 생성
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "새로운 Access Token 생성");

        return result;
    }

    // logout ( 쿠키 + Redis 삭제 )
    public void logout(Integer userId, HttpServletRequest request, HttpServletResponse response) {
        try{
            String refreshToken = cookieUtil.getCookieValue(request, "refreshToken").orElse(null);

            // Redis에서 refresh 삭제
            if(refreshToken != null) {
                String tokenKey =  REFRESH_TOKEN_PREFIX + userId + ":" +  refreshToken;
                redisTemplate.delete(tokenKey);
            }

            // 쿠키 삭제
            cookieUtil.deleteAccessTokenCookie(response);
            cookieUtil.deleteRefreshTokenCookie(response);
            
            log.info("로그아웃 완료 : userId={}", userId);
            
        } catch(Exception e){
            log.info("로그아웃 에러 : userId={}", userId);
            throw new RuntimeException(e.getMessage());
        }
    }

    // JWT 토큰 생성 및 쿠키에 저장 ( 로그인 )
    public void createTokensForUser(Users user, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getName(),
                user.getId()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        storeRefreshToken(refreshToken, user.getId());

        cookieUtil.createAccessTokenCookie(response, accessToken);
        cookieUtil.createRefreshTokenCookie(response, refreshToken);

        log.info("토큰 생성 및 Redis + 쿠키에 저장 완료 : userId={}", user.getId());
    }


    private void storeRefreshToken(String refreshToken, Integer userId) {
        String tokenKey = REFRESH_TOKEN_PREFIX + userId + ":" + refreshToken;
        redisTemplate.opsForValue().set(
                tokenKey,
                "valid",
                REFRESH_TOKEN_EXPIRE_TIME,
                TimeUnit.DAYS
        );
        log.debug("Redis에 Refresh Token 저장: userId={}", userId);
    }
}
