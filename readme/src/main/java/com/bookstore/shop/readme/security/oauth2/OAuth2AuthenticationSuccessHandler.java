package com.bookstore.shop.readme.security.oauth2;

import com.bookstore.shop.readme.domain.RefreshToken;
import com.bookstore.shop.readme.repository.RefreshTokenRepository;
import com.bookstore.shop.readme.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Long   memberId = (Long)   oAuth2User.getAttribute("memberId");
        String role     = (String) oAuth2User.getAttribute("role");

        String accessToken  = jwtUtil.generateAccessToken(memberId, role);
        String refreshToken = jwtUtil.generateRefreshToken(memberId);

        refreshTokenRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        rt -> rt.rotate(refreshToken, jwtUtil.getRefreshTokenExpiration()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.create(memberId, refreshToken,
                                        jwtUtil.getRefreshTokenExpiration()))
                );

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("accessToken",  accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
