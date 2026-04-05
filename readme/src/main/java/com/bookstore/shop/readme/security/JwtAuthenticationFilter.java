package com.bookstore.shop.readme.security;

import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistStore blacklistStore;
    // 로그아웃된 AccessToken 블랙리스트 (운영에서는 Redis 사용 권장)

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            if (blacklistStore.contains(token)) {
                sendError(response, "로그아웃된 토큰입니다.");
                return;
            }
            try {
                if (jwtUtil.validateToken(token)) {
                    Long memberId = jwtUtil.getMemberId(token);
                    String roleStr = jwtUtil.getRole(token);

                    // JWT 클레임으로 CustomUserDetails 생성 (DB 조회 없음 — Stateless JWT)
                    MemberRole memberRole = MemberRole.valueOf(roleStr);
                    CustomUserDetails userDetails = new CustomUserDetails(memberId, memberRole);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,              // principal: CustomUserDetails
                                    null,                     // credentials: JWT 방식에서는 불필요
                                    userDetails.getAuthorities()
                            );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                sendError(response, "만료된 토큰입니다.");
                return;
            } catch (IllegalArgumentException e) {
                // JWT에 알 수 없는 role 값이 포함된 경우 (정상 토큰이라면 발생하지 않음)
                sendError(response, "유효하지 않은 토큰입니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
