package com.bookstore.shop.readme.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * SecurityContext에 저장된 현재 로그인 회원의 ID를 반환합니다.
     * JwtAuthenticationFilter가 토큰 검증 후 SecurityContext에
     * memberId를 name으로 저장하므로 getName()으로 꺼냅니다.
     *
     * [구조도 준수] util.SecurityUtil — 현재 로그인 사용자 조회
     * [JwtUtil 연계] JwtUtil.getMemberId(token)으로 파싱된 값이
     *               Authentication.name에 저장된 상태를 전제합니다.
     */
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("회원 ID 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 현재 로그인 여부만 확인할 때 사용합니다.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null;
    }
}