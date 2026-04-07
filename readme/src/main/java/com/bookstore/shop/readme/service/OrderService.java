package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.OrderCreateRequest;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderListResponse;
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
@Transactional
public class OrderService {

    private final CartRepository      cartRepository;
    private final CartItemRepository  cartItemRepository;
    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository   productRepository;

    // ── 마이페이지 주문 조회 ────────────────────────────────────────────────

    /** 내 주문 목록 (페이징, 최신순) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(Long memberId, Pageable pageable) {
        Page<OrderListResponse> result = orderRepository
                .findByMemberId(memberId, pageable)
                .map(OrderListResponse::new);
        return ResponseEntity.ok(result);
    }

    /** 내 주문 상세 (본인 주문만 조회 가능) */
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    // ── 장바구니 → 주문 생성 ────────────────────────────────────────────────

    /**
     * 체크된 CartItem → Order + OrderItem 생성
     * 재고 확인 후 totalPrice / discountAmount / finalPrice 계산
     * 성공 시 orderId 반환 (프론트 orderStore.setOrder() 용)
     */
    public ResponseEntity<Long> createOrder(Long memberId, OrderCreateRequest req) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다."));

        List<CartItem> checkedItems = cartItemRepository.findByCartId(cart.getId())
                .stream().filter(CartItem::isChecked).toList();

        if (checkedItems.isEmpty()) {
            throw new RuntimeException("선택된 상품이 없습니다.");
        }

        // 재고 확인
        for (CartItem ci : checkedItems) {
            Product p = ci.getProduct();
            if (p.getProductStatus() != ProductStatus.ACTIVATE)
                throw new RuntimeException("판매 중단된 상품이 포함되어 있습니다: " + p.getTitle());
            if (p.getStock() < ci.getQuantity())
                throw new RuntimeException("재고가 부족합니다: " + p.getTitle());
        }

        // 금액 계산
        int totalPrice = checkedItems.stream()
                .mapToInt(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();
        int saleTotal = checkedItems.stream()
                .mapToInt(ci -> ci.getProduct().getSalePrice() * ci.getQuantity()).sum();
        int discountAmount = totalPrice - saleTotal;

        // 주문 생성
        Member member = cart.getMember();
        Order order = Order.builder()
                .member(member)
                .number("ORD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .discountAmount(discountAmount)
                .finalPrice(saleTotal)
                .receiverName(req.receiverName())
                .receiverPhone(req.receiverPhone())
                .deliveryAddress(req.deliveryAddress())
                .deliveryAddressDetail(req.deliveryAddressDetail())
                .deliveryZipCode(req.deliveryZipCode())
                .deliveryMemo(req.deliveryMemo())
                .build();
        orderRepository.save(order);

        // OrderItem 생성 + 재고 차감
        for (CartItem ci : checkedItems) {
            Product p = ci.getProduct();
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productTitle(p.getTitle())
                    .productAuthor(p.getAuthor())
                    .salePrice(p.getSalePrice())
                    .quantity(ci.getQuantity())
                    .itemTotal(p.getSalePrice() * ci.getQuantity())
                    .build();
            orderItemRepository.save(oi);

            p.setStock(p.getStock() - ci.getQuantity());
            productRepository.save(p);
        }

        // 구매 완료된 CartItem 삭제
        cartItemRepository.deleteAll(checkedItems);

        return ResponseEntity.ok(order.getId());
    }

    // ── 관리자용 전체 주문 목록 ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(Pageable pageable) {
        Page<OrderListResponse> result = orderRepository.findAll(pageable)
                .map(OrderListResponse::new);
        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    /** 주문 상태 변경 (관리자) */
    public ResponseEntity<String> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setOrderStatus(OrderStatus.valueOf(status));
        return ResponseEntity.ok("주문 상태가 변경되었습니다.");
    }
}
