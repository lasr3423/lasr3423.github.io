package com.bookstore.shop.readme.security.oauth2;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.service.OAuthMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuthMemberService oAuthMemberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String       registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthUserInfo userInfo = switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
            default       -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자: " + registrationId);
        };

        Member member = oAuthMemberService.findOrCreate(
                userInfo.getProvider(), userInfo.getProviderId(),
                userInfo.getEmail(), userInfo.getName()
        );

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getMemberRole().name())),
                Map.of(
                        "memberId", member.getId(),
                        "role",     member.getMemberRole().name()
                ),
                "memberId"
        );
    }
}
