package com.handi.backend.util;

import com.handi.backend.entity.Users;
import com.handi.backend.repository.UsersRepository;
import com.handi.backend.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


// 인증 성공 후 사용자를 Redirect 하도록
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authservice;
    private final UsersRepository usersRepository;


    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        // 응답 이미 커밋 = 리다이렉트 불가
        if (response.isCommitted()) {
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }


    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {


        // 임시로 로그인 성공하면 저 주소로 리다이렉트
        String authorizedRedirectUri =  "http://localhost:8080/swagger-ui.html";

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            Integer userId = (Integer) oAuth2User.getAttributes().get("userId");
            String userEmail = (String) oAuth2User.getAttributes().get("userEmail");
            String userName = (String) oAuth2User.getAttributes().get("userName");

            Users user = usersRepository.findById(userId).orElse(null);

            if (user == null) {
                log.error("OAuth2 로그인 성공했지만 사용자를 찾을 수 없습니다: {}", userEmail);
                return UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                        .queryParam("error", "user_not_found")
                        .build().toUriString();
            }

            // JWT 토큰 생성 및 쿠키에 저장 ( 로그인 )
            authservice.createTokensForUser(user, response);

            // 클라이언트로 성공 정보와 함께 리다이렉트
            String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                    .queryParam("success", "true")
                    .queryParam("userId", userId)
                    .queryParam("name", URLEncoder.encode(userName != null ? userName : "", StandardCharsets.UTF_8))
                    .build().toUriString();

            log.info("OAuth2 로그인 성공 - userId: {}, email: {}, 리다이렉트: {}", userId, userEmail, authorizedRedirectUri);
            return targetUrl;

        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 오류 발생: {}", e.getMessage());
            return UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                    .queryParam("error", "authentication_processing_failed")
                    .queryParam("message", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .build().toUriString();
        }
    }
}