package com.handi.backend.dto.oauth.response;

public interface OAuth2Response {
    String getSocialProvider();

    String getId();

    String getEmail();

    String getName();


}
