package com.bookstore.shop.readme.controller.mypage;

import com.bookstore.shop.readme.dto.request.ChangePasswordRequest;
import com.bookstore.shop.readme.dto.request.UpdateMemberRequest;
import com.bookstore.shop.readme.dto.response.MemberResponse;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderListResponse;
import com.bookstore.shop.readme.dto.response.PaymentStatusResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.MemberService;
import com.bookstore.shop.readme.service.OrderService;
import com.bookstore.shop.readme.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// [수정] PaymentRepository 직접 주입 제거 → PaymentService 위임으로 변경
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MyPageApiController {

    private final MemberService  memberService;
    private final OrderService   orderService;
    private final PaymentService paymentService;

    // ── 회원 정보 ───────────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails user) {
        return memberService.getMyInfo(user.getMemberId());
    }

    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateInfo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UpdateMemberRequest req) {
        return memberService.updateInfo(user.getMemberId(), req);
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ChangePasswordRequest req) {
        return memberService.changePassword(user.getMemberId(), req);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal CustomUserDetails user) {
        return memberService.withdraw(user.getMemberId());
    }

    // ── 주문 내역 ───────────────────────────────────────────────────────────

    @GetMapping("/me/orders")
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getMyOrders(user.getMemberId(), pageable);
    }

    @GetMapping("/me/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long orderId) {
        return orderService.getMyOrderDetail(user.getMemberId(), orderId);
    }

    // ── 결제 내역 (/mypage/payment 페이지 연동) ─────────────────────────────

    @GetMapping("/me/payments")
    public ResponseEntity<Page<PaymentStatusResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.getMyPayments(user.getMemberId(), pageable);
    }
}
