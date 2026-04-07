package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.OrderCreateRequest;
import com.bookstore.shop.readme.dto.response.OrderCreateResponse;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderListResponse;
import com.bookstore.shop.readme.dto.response.OrderSummaryResponse;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository  cartItemRepository;
    private final MemberRepository memberRepository;
    private final PaymentService      paymentService; // 주문 취소 시 환불 처리

    /*
     * 장바구니 -> 주문 생성
     * 체크된 CartItem 목록을 받아서 Order + OrderItem 레코드 생성
     * 재고 확인 (ProductStatus.ACTIVE + 수량)
     * totalPrice, discountAmount, finalPrice 계산 후 Order 저장
     * 성공 시 orderId 반환 -> 프론트에서 orderStore.setOrder() 호출에 사용
     */

    // ── 주문 생성 (장바구니 → 주문) ──────────────────────────────────────
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long memberId) {

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 2. 체크된 장바구니 항목 조회
        // JpaRepository 기본 제공 findAllById() 사용 — IDs 목록으로 한번에 조회
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("주문할 상품이 없습니다.");
        }

        // 3. 금액 계산
        // ⚠️ Product의 판매가는 salePrice — price(정가) 아님
        int totalPrice = cartItems.stream()
                .mapToInt(item -> item.getProduct().getSalePrice() * item.getQuantity())
                .sum();
        int discountAmount = 0; // 현재 할인 없음 (쿠폰/포인트 기능 추가 시 계산)
        int finalPrice = totalPrice - discountAmount;

        // 4. Order 생성
        // ⚠️ Order 엔티티의 주문번호 필드는 "number" — "orderNumber" 아님
        // ⚠️ OrderStatus 초기값은 "PENDING" — READY 아님
        Order order = Order.builder()
                .member(member)
                .number(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryAddressDetail(request.getDeliveryAddressDetail())
                .deliveryZipCode(request.getDeliveryZipCode())
                .deliveryMemo(request.getDeliveryMemo())
                .build();
        orderRepository.save(order);

        // 5. OrderItem 생성 — 주문 당시 상품 정보를 직접 저장 (나중에 상품이 바뀌어도 보존)
        // ⚠️ OrderItem 필드: productTitle, productAuthor, salePrice, itemTotal
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int itemTotal = product.getSalePrice() * cartItem.getQuantity();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productTitle(product.getTitle())   // 주문 당시 상품명 스냅샷
                    .productAuthor(product.getAuthor()) // 주문 당시 저자 스냅샷
                    .salePrice(product.getSalePrice())  // 주문 당시 판매가 스냅샷
                    .quantity(cartItem.getQuantity())
                    .itemTotal(itemTotal)               // 소계 (salePrice × quantity)
                    .build();
            orderItemRepository.save(orderItem);
        }

        // 6. 주문 완료된 CartItem 삭제
        cartItemRepository.deleteAll(cartItems);

        // 7. 결제 화면에 전달할 대표 상품명 생성
        String itemName = cartItems.get(0).getProduct().getTitle()
                + (cartItems.size() > 1 ? " 외 " + (cartItems.size() - 1) + "건" : "");

        // 8. orderId + finalPrice + itemName 반환 → 프론트에서 orderStore.setOrder() 호출
        return new OrderCreateResponse(order.getId(), finalPrice, itemName);
    }

    // ── 주문 목록 조회 (마이페이지) ──────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrderList(Long memberId, Pageable pageable) {
        // Page<Order> → Page<OrderSummaryResponse> 변환
        return orderRepository.findByMemberId(memberId, pageable)
                .map(OrderSummaryResponse::new);
    }

    // ── 주문 상세 조회 ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, Long memberId) {
        // memberId 같이 검증 → 타인의 주문 조회 방지
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return new OrderDetailResponse(order, items);
    }

    // ── 마이페이지 전용 메서드 ────────────────────────────────────────────

    /** 내 주문 목록 (마이페이지용 — ResponseEntity 포장, OrderListResponse 반환) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(Long memberId, Pageable pageable) {
        return ResponseEntity.ok(
                orderRepository.findByMemberId(memberId, pageable).map(OrderListResponse::new)
        );
    }

    /** 내 주문 상세 (마이페이지용 — memberId 본인 검증 포함, ResponseEntity 포장) */
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    // ── 관리자 전용 메서드 ────────────────────────────────────────────────

    /** 전체 주문 목록 (관리자용 — memberId 검증 없음) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderRepository.findAll(pageable).map(OrderListResponse::new));
    }

    /** 주문 상세 조회 (관리자용 — memberId 검증 없음) */
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    /** 주문 상태 변경 (관리자용) */
    @Transactional
    public ResponseEntity<String> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setOrderStatus(OrderStatus.valueOf(status));
        return ResponseEntity.ok("주문 상태가 변경되었습니다.");
    }

    // ── 주문 취소 + PG사 환불 ────────────────────────────────────────────
    @Transactional
    public void cancelOrder(Long orderId, Long memberId, String cancelReason) {
        // 1. 본인 주문인지 확인
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // ⚠️ OrderStatus.CANCELED — CANCEL 아님
        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new RuntimeException("이미 취소된 주문입니다.");
        }

        // 2. PAYED 상태일 때만 PG사 환불 처리
        // PENDING(결제 전) 상태에서 취소하면 환불 필요 없음
        if (order.getOrderStatus() == OrderStatus.PAYED) {
            paymentService.cancelPayment(orderId, cancelReason);
        }

        // 3. 주문 상태 취소로 변경 + 취소 일시 기록
        order.setOrderStatus(OrderStatus.CANCELED);
        order.setCancelledAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
    }
}
