package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.RefreshToken;
import com.bookstore.shop.readme.dto.KakaoUserInfo;
import com.bookstore.shop.readme.repository.RefreshTokenRepository;
import com.bookstore.shop.readme.service.KakaoOAuthService;
import com.bookstore.shop.readme.service.OAuthMemberService;
import com.bookstore.shop.readme.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/oauth/kakao")
@RequiredArgsConstructor
public class KakaoOAuthApiController {

    @Value("${kakao.client-id}")      private String clientId;
    @Value("${kakao.redirect-uri}")   private String redirectUri;
    @Value("${app.oauth2.redirect-uri}") private String frontendRedirectUri;

    private final KakaoOAuthService  kakaoOAuthService;
    private final OAuthMemberService oAuthMemberService;
    private final JwtUtil            jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    /* 카카오 인증 URL 반환 */
    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=profile_nickname,account_email";

        return ResponseEntity.ok(Map.of("authUrl", kakaoAuthUrl));
    }

    // 카카오 콜백 처리
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            HttpServletResponse response) throws IOException {

        // 1단계: code → 카카오 AccessToken
        String kakaoToken = kakaoOAuthService.getKakaoAccessToken(code);
        // 2단계: 카카오 AccessToken → 사용자 정보
        KakaoUserInfo userInfo = kakaoOAuthService.getKakaoUserInfo(kakaoToken);

        // 회원 조회 or 자동 가입
        Member member = oAuthMemberService.findOrCreate(
                AuthProvider.KAKAO,
                String.valueOf(userInfo.getId()),
                userInfo.getEmail(),
                userInfo.getNickname()
        );

        // JWT 발급
        String accessToken  = jwtUtil.generateAccessToken(member.getId(), member.getMemberRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        rt -> rt.rotate(refreshToken, jwtUtil.getRefreshTokenExpiration()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.create(member.getId(), refreshToken,
                                        jwtUtil.getRefreshTokenExpiration()))
                );

        // 프론트엔드로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("accessToken",  accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        response.sendRedirect(targetUrl);
        return ResponseEntity.status(302).build();
    }
}
