package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    // 로그인
    // 회원가입
    // 로그아웃
}
