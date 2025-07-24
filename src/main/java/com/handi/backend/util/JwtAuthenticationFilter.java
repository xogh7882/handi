package com.handi.backend.util;

import com.handi.backend.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


// OncePerRequestFilter = 한 번의 Request에 대해 한번만 실행되는 Filter
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final AuthService authService;

    // 인증이 필요없는 경로
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/swagger-ui",
            "/api-docs",
            "/login",
            "/oauth2",
            "/auth/refresh",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("JWT 필터 처리 중: {}", requestURI);

        // 제외 경로 확인
        if (isExcludedPath(requestURI)) {
            log.debug("제외 경로이므로 인증 스킵: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 1. Access Token 확인
            String accessToken = cookieUtil.getCookieValue(request, "accessToken").orElse(null);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // Access Token 유효
                log.debug("Access Token 유효 - 인증 성공");
                setAuthenticationContext(request, accessToken);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Access Token이 없거나 만료된 경우 - Refresh Token 확인
            String refreshToken = cookieUtil.getCookieValue(request, "refreshToken").orElse(null);

            if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
                // Refresh Token으로 새로운 Access Token 발급 시도
                log.debug("Access Token 만료, Refresh Token으로 재발급 시도");
                boolean refreshSuccess = authService.refreshAccessToken(request, response);

                if (refreshSuccess) {
                    // 새로운 Access Token으로 인증 설정
                    String newAccessToken = cookieUtil.getCookieValue(request, "accessToken").orElse(null);
                    if (newAccessToken != null) {
                        setAuthenticationContext(request, newAccessToken);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }

            // 3. 모든 토큰이 유효하지 않은 경우 - 인증 실패
            log.debug("인증 실패 - 로그인 필요");
            handleAuthenticationFailure(response);

        } catch (Exception e) {
            log.error("JWT 필터 처리 중 오류 발생: {}", e.getMessage());
            handleAuthenticationFailure(response);
        }
    }

    private boolean isExcludedPath(String requestURI) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    private void setAuthenticationContext(HttpServletRequest request, String accessToken) {
        try {
            Integer userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            String email = jwtTokenProvider.getEmailFromToken(accessToken);
            String name = jwtTokenProvider.getNameFromToken(accessToken);

            // Request에 사용자 정보 설정 (Controller에서 사용 가능)
            request.setAttribute("userId", userId);
            request.setAttribute("userEmail", email);
            request.setAttribute("userName", name);

            log.debug("인증 컨텍스트 설정 완료 - userId: {}, email: {}", userId, email);

        } catch (Exception e) {
            log.error("인증 컨텍스트 설정 실패: {}", e.getMessage());
        }
    }

    private void handleAuthenticationFailure(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"로그인이 필요합니다.\"}");
    }/**/
}
