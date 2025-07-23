package com.handi.backend.dto.oauth.response;

import lombok.RequiredArgsConstructor;

import java.util.Map;

public class KakaoResponse implements  OAuth2Response{

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> properties;


    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.properties = (Map<String, Object>) attributes.get("properties");
    }

    @Override
    public String getSocialProvider() {
        return "kakao";
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        if (properties == null) return null;
        return (String) properties.get("nickname");
    }
}
