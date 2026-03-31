package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.dto.request.*;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// @RestController : @Controller + @ResponseBody 합친 어노테이션
//                   모든 메서드 반환값을 자동으로 JSON으로 직렬화하여 HTTP 응답
@RestController
@RequiredArgsConstructor
// "/order/payment" prefix를 이 컨트롤러의 모든 엔드포인트에 적용
@RequestMapping("/order/payment")
public class MyPaymentApiController {

    private final PaymentService paymentService;

    // ── POST /order/payment/ready ─────────────────────────────────────────
    // 카카오/네이버 결제 준비: PG사 API에서 tid + 결제창 URL 발급
    // 프론트에서 이 응답의 redirectUrl로 location.href 이동
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> readyPayment(
            @RequestBody PaymentReadyRequest request,
            // @AuthenticationPrincipal : Spring Security가 JWT 토큰을 파싱해서
            //                            SecurityContext에 저장한 Member 객체를 자동 주입
            @AuthenticationPrincipal Member member) {

        PaymentReadyResponse response = paymentService.requestReady(request, member.getId());
        return ResponseEntity.ok(response); // HTTP 200 + JSON 응답
    }

    // ── POST /order/payment/confirm ───────────────────────────────────────
    // 토스 결제 최종 승인
    // 토스 successUrl 리다이렉트 이후 프론트에서 paymentKey, orderId, amount 전송
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.confirmToss(request, member.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // ── POST /order/payment/approve ───────────────────────────────────────
    // 카카오/네이버 결제 최종 승인
    // approval_url(카카오)/returnUrl(네이버) 리다이렉트 이후 pg_token/paymentId 전송
    @PostMapping("/approve")
    public ResponseEntity<String> approvePayment(
            @RequestBody PaymentApproveRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.approvePayment(request, member.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // ── POST /order/payment/fail ──────────────────────────────────────────
    // 토스 결제 실패 처리: failUrl 리다이렉트 이후 code, message 전송
    // ⚠️ 인증 없이 호출 가능해야 함 → SecurityConfig에서 permitAll() 추가 필요
    //    (결제 실패 시 비로그인 상태가 될 수 있음)
    @PostMapping("/fail")
    public ResponseEntity<String> failPayment(@RequestBody PaymentFailRequest request) {
        paymentService.failPayment(request);
        return ResponseEntity.ok("결제에 실패했습니다.");
    }
}