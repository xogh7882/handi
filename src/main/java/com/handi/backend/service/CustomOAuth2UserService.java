package com.handi.backend.service;

import com.handi.backend.dto.oauth.response.GoogleResponse;
import com.handi.backend.dto.oauth.response.KakaoResponse;
import com.handi.backend.dto.oauth.response.NaverResponse;
import com.handi.backend.dto.oauth.response.OAuth2Response;
import com.handi.backend.entity.Users;
import com.handi.backend.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UsersRepository usersRepository;

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 프랫폼 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 플랫폼별 파싱
        OAuth2Response oAuth2Response = getOAuth2Response(registrationId, oAuth2User.getAttributes());

        if(oAuth2Response == null){
            log.error("지원하지 않는 OAuth2 플랫폼: {}", registrationId);
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 플랫폼입니다.");
        }

        // 4. 필수 정보 검증
        if(oAuth2Response.getEmail() == null || oAuth2Response.getEmail().isEmpty()){
            log.error("OAuth2 응답에서 이메일을 찾을 수 없습니다: platform={}", registrationId);
            throw new OAuth2AuthenticationException("이메일 정보가 필요합니다.");
        }

        // 5. 사용자 정보 분리 ( 신규 or 기존 )
        Users user = processUser(oAuth2Response);

        // 6. OAuth2User 객체 생성
        return createOAuth2User(user, oAuth2User.getAttributes());
    }

    // OAuth2User 객체 생성 ( Spring Security 에서 사용 )
    private OAuth2User createOAuth2User(Users user, Map<String, Object> originalattributes) {
        Map<String, Object> attributes = new HashMap<>(originalattributes);
        attributes.put("userId", user.getId());
        attributes.put("userEmail", user.getEmail());
        attributes.put("userName", user.getName());

        return new OAuth2User() {
            @Override
            public Map<String, Object> getAttributes() {
                return attributes;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
//                return Collections.singletonList(
//                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
//                );
                return null;
            }

            @Override
            public String getName() {
                return user.getName();
            }
        };

    }

    private Users processUser(OAuth2Response oAuth2Response) {
        String email = oAuth2Response.getEmail();

        Users finduser = usersRepository.findByEmailAndSocialProvider(email, oAuth2Response.getSocialProvider());
        if(finduser == null){
            log.info("신규 사용자 : email={}", email);
            return createNewUser(oAuth2Response);
        }else {
            log.info("기존 사용자 : email={}", email);
            return finduser;
        }
    }

    private OAuth2Response getOAuth2Response(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleResponse(attributes);

            case "naver":
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                return new NaverResponse(response);

            case "kakao":
                return new KakaoResponse(attributes);

            default:
                return null;
        }
    }


    // 새로운 사용자 생성
    private Users createNewUser(OAuth2Response oAuth2Response) {
        Users user = new Users();
        user.setEmail(oAuth2Response.getEmail());
        user.setName(oAuth2Response.getName());
        user.setSocialProvider(oAuth2Response.getSocialProvider());

        usersRepository.save(user);
        return user;
    }

}

