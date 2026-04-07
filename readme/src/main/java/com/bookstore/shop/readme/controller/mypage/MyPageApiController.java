package com.bookstore.shop.readme.controller.mypage;

import com.bookstore.shop.readme.dto.request.ChangePasswordRequest;
import com.bookstore.shop.readme.dto.request.UpdateMemberRequest;
import com.bookstore.shop.readme.dto.response.MemberResponse;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderListResponse;
import com.bookstore.shop.readme.dto.response.PaymentStatusResponse;
import com.bookstore.shop.readme.repository.PaymentRepository;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.MemberService;
import com.bookstore.shop.readme.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MyPageApiController {

    private final MemberService     memberService;
    private final OrderService      orderService;
    private final PaymentRepository paymentRepository; // [신규] 결제 내역 직접 조회

    // ── 회원 정보 ───────────────────────────────────────────────────────────

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails user) {
        return memberService.getMyInfo(user.getMemberId());
    }

    /** 회원 정보 수정 (이름 / 전화번호 / 주소) */
    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateInfo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UpdateMemberRequest req) {
        return memberService.updateInfo(user.getMemberId(), req);
    }

    /** 비밀번호 변경 */
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ChangePasswordRequest req) {
        return memberService.changePassword(user.getMemberId(), req);
    }

    /** 회원 탈퇴 (soft delete) */
    @DeleteMapping("/me")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal CustomUserDetails user) {
        return memberService.withdraw(user.getMemberId());
    }

    // ── 주문 내역 ───────────────────────────────────────────────────────────

    /** 내 주문 목록 (페이징) */
    @GetMapping("/me/orders")
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getMyOrders(user.getMemberId(), pageable);
    }

    /** 내 주문 상세 */
    @GetMapping("/me/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long orderId) {
        return orderService.getMyOrderDetail(user.getMemberId(), orderId);
    }

    // ── 결제 내역 (/mypage/payment 페이지 연동) ─────────────────────────────

    /** [신규] 내 결제 내역 목록 (최신순, 페이징) */
    @GetMapping("/me/payments")
    public ResponseEntity<Page<PaymentStatusResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentStatusResponse> result =
                paymentRepository.findByOrder_MemberId(user.getMemberId(), pageable)
                        .map(PaymentStatusResponse::new);
        return ResponseEntity.ok(result);
    }
}
