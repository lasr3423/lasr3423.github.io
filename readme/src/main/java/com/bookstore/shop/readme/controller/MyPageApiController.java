package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MyPageApiController {
    private final MemberService memberService;
//    private final OrderService orderService;
//    private final PaymentService paymentService;
//    private final QnaService qnaService;
//    private final ReviewService reviewService;

    // 마이페이지
    // 회원 정보 수정
    // 비밀번호 변경
    // 회원 탈퇴
}