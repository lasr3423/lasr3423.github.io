package com.bookstore.shop.readme.security.oauth2;

import com.bookstore.shop.readme.domain.AuthProvider;

public interface OAuthUserInfo {
    String       getProviderId();
    String       getEmail();
    String       getName();
    AuthProvider getProvider();
}
