package com.bookstore.shop.readme.controller.mypage;

import com.bookstore.shop.readme.dto.request.ChangePasswordRequest;
import com.bookstore.shop.readme.dto.request.QnAUpdateRequest;
import com.bookstore.shop.readme.dto.request.ReviewUpdateRequest;
import com.bookstore.shop.readme.dto.request.UpdateMemberRequest;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.*;
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

    private final MemberService  memberService;
    private final OrderService   orderService;
    private final PaymentService paymentService;
    private final ReviewService  reviewService;
    private final QnAService     qnaService;

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

    // ── 내 리뷰 관리 (/mypage/review) ──────────────────────────────────────

    // 내 리뷰 목록 — REQ-M-016
    @GetMapping("/me/reviews")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.getMyReviews(user.getMemberId(), pageable);
    }

    // 내 리뷰 상세 — REQ-M-017
    @GetMapping("/me/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getMyReviewDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long reviewId) {
        return reviewService.getMyReviewDetail(reviewId, user.getMemberId());
    }

    // 내 리뷰 수정 — REQ-M-018
    @PutMapping("/me/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateMyReview(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest req) {
        return reviewService.updateReview(reviewId, req, user.getMemberId());
    }

    // 내 리뷰 삭제 — REQ-M-019
    @DeleteMapping("/me/reviews/{reviewId}")
    public ResponseEntity<String> deleteMyReview(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId, user.getMemberId());
    }

    // ── 내 QnA 관리 (/mypage/qna) ──────────────────────────────────────────

    // 내 QnA 목록 — REQ-M-021
    @GetMapping("/me/qnas")
    public ResponseEntity<Page<QnAResponse>> getMyQnAs(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return qnaService.getMyQnAs(user.getMemberId(), pageable);
    }

    // 내 QnA 상세 — REQ-M-022
    @GetMapping("/me/qnas/{qnaId}")
    public ResponseEntity<QnAResponse> getMyQnADetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long qnaId) {
        return qnaService.getMyQnADetail(qnaId, user.getMemberId());
    }

    // 내 QnA 수정 — REQ-M-023
    @PutMapping("/me/qnas/{qnaId}")
    public ResponseEntity<QnAResponse> updateMyQnA(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long qnaId,
            @RequestBody QnAUpdateRequest req) {
        return qnaService.updateQnA(qnaId, req, user.getMemberId());
    }

    // 내 QnA 삭제 — REQ-M-024
    @DeleteMapping("/me/qnas/{qnaId}")
    public ResponseEntity<String> deleteMyQnA(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long qnaId) {
        return qnaService.deleteQnA(qnaId, user.getMemberId());
    }
}
