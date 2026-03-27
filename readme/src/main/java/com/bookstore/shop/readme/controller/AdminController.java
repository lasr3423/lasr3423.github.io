package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {
    private final MemberRepository memberRepository;
    // 관리자 페이지
    // 회원 목록
    // 회원 상태
    // 회원 등급
}
