package com.bookstore.shop.readme.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfo {
    private Long id;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String  email;
        private Profile profile;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
        }
    }

    public String getEmail() {
        return (kakaoAccount != null) ? kakaoAccount.getEmail() : null;
    }

    public String getNickname() {
        if (kakaoAccount == null || kakaoAccount.getProfile() == null) return "카카오사용자";
        return kakaoAccount.getProfile().getNickname();
    }
}
