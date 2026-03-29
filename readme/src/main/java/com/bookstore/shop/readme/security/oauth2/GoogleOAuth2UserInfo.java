package com.bookstore.shop.readme.security.oauth2;

import com.bookstore.shop.readme.domain.AuthProvider;

import java.lang.Object;
import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuthUserInfo {
    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
}
