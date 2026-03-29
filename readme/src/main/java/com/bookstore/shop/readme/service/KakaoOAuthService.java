package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoOAuthService {

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.client-secret:}")
    private String clientSecret;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;   // ← 빈 주입으로 변경

    /* 1단계: Authorization Code → 카카오 AccessToken */
    public String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type",   "authorization_code");
        params.add("client_id",    clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code",         code);
        if (!clientSecret.isBlank()) params.add("client_secret", clientSecret);

        try {
            // raw type Map → Map<String, Object> 로 변경
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    new HttpEntity<>(params, headers),
                    new ParameterizedTypeReference<>() {}
            );

            // getBody() null 체크 추가
            Map<String, Object> body = res.getBody();
            if (body == null || !body.containsKey("access_token"))
                throw new RuntimeException("카카오 응답에 access_token이 없습니다.");

            return (String) body.get("access_token");

        } catch (RuntimeException e) {
            throw e;   // 위에서 던진 예외는 그대로 전파
        } catch (Exception e) {
            log.error("카카오 토큰 교환 실패", e);
            throw new RuntimeException("카카오 액세스 토큰 발급 실패");
        }
    }

    /* 2단계: 카카오 AccessToken → 사용자 정보 */
    public KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);

        try {
            KakaoUserInfo userInfo = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoUserInfo.class
            ).getBody();

            // getBody() null 체크 추가
            if (userInfo == null)
                throw new RuntimeException("카카오 사용자 정보가 비어있습니다.");

            return userInfo;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }
    }
}