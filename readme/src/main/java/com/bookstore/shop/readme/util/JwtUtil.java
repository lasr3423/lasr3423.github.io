package com.bookstore.shop.readme.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long      accessTokenExpiration;
    private final long      refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessExp,
            @Value("${jwt.refresh-token-expiration}") long refreshExp
    ) {
        this.secretKey              = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration  = accessExp;
        this.refreshTokenExpiration = refreshExp;
    }

    /* AccessToken 생성 */
    public String generateAccessToken(Long memberId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    /* RefreshToken 생성 */
    public String generateRefreshToken(Long memberId) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    /* Claims 파싱 */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long   getMemberId(String token) { return Long.parseLong(parseClaims(token).getSubject()); }
    public String getRole(String token)     { return parseClaims(token).get("role", String.class); }

    /* 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRefreshTokenExpiration() { return refreshTokenExpiration; }
}
