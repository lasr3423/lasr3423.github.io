package com.bookstore.shop.readme.controller.mypage;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.service.MemberService;
import com.bookstore.shop.readme.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MyPageApiController {
    private final MemberService memberService;
    private final PaymentService paymentService;
//    private final OrderService orderService;
//    private final QnaService qnaService;
//    private final ReviewService reviewService;

    // 마이페이지
    @PostMapping("/mypage")
    public ResponseEntity<String> getMyPage(Member member) {
    }

    // 회원 정보 수정
    @PutMapping("/mypage/{id}")
    public ResponseEntity<Member> updateInfo(UpdateRequest, Member) {

    }

    // 비밀번호 변경
    @PutMapping("/mypage/{id}")
    public ResponseEntity<Member> updatePassword(UpdateRequest, Member) {

    }

    // 회원 탈퇴
    @PatchMapping("/mypage/{id}")
    public ResponseEntity<String> withdraw(Long) {

    }
}