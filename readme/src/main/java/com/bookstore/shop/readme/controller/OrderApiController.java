package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.OrderCreateRequest;
import com.bookstore.shop.readme.dto.response.OrderCreateResponse;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderSummaryResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
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
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;

    // ── POST /api/order ────────────────────────────────────────────────────
    // 장바구니 → 주문 생성
    // 응답: { orderId, finalPrice, itemName }
    // → 프론트에서 orderStore.setOrder(orderId, finalPrice, itemName) 호출
    // → router.push('/payment') 로 이동
    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderCreateResponse result = orderService.createOrder(request, userDetails.getMemberId());
        return ResponseEntity.ok(result);
    }

    // ── GET /api/order ─────────────────────────────────────────────────────
    // 마이페이지 주문 목록 조회 (페이징)
    // @PageableDefault : 기본 10개, 최신순 (orderAt 기준)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getOrderList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "orderAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        Page<OrderSummaryResponse> result =
                orderService.getOrderList(userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(result);
    }

    // ── GET /api/order/{orderId} ───────────────────────────────────────────
    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDetailResponse result =
                orderService.getOrderDetail(orderId, userDetails.getMemberId());
        return ResponseEntity.ok(result);
    }

    // ── DELETE /api/order/{orderId} ────────────────────────────────────────
    // 주문 취소 + PG사 환불
    // @RequestBody(required = false) : body 없이 호출해도 에러 안 남
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequest cancelRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String reason = (cancelRequest != null && cancelRequest.cancelReason() != null)
                ? cancelRequest.cancelReason()
                : "고객 요청";

        orderService.cancelOrder(orderId, userDetails.getMemberId(), reason);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }

    // 취소 사유 요청 body — record로 간단하게 처리
    public record CancelOrderRequest(String cancelReason) {}

}
