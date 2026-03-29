package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.RefreshRequest;
import com.bookstore.shop.readme.dto.SigninRequest;
import com.bookstore.shop.readme.dto.SignupRequest;
import com.bookstore.shop.readme.dto.TokenResponse;
import com.bookstore.shop.readme.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {
    private final MemberService memberService;
    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequest req) {
        return memberService.signup(req);
    }
    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signin(@RequestBody @Valid SigninRequest req) {
        return memberService.signin(req);
    }

    // 로그아웃
    @PostMapping("/signout")
    public ResponseEntity<String> signout(HttpServletRequest request) {
        return memberService.signout(extractToken(request));
    }

    // 갱신
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest req) {
        return memberService.refresh(req.getRefreshToken());
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        throw new RuntimeException("토큰이 없습니다.");
    }
}
