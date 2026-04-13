package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Delivery;
import com.bookstore.shop.readme.domain.DeliveryStatus;
import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderItem;
import com.bookstore.shop.readme.domain.OrderStatus;
import com.bookstore.shop.readme.domain.Payment;
import com.bookstore.shop.readme.domain.PaymentStatus;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.dto.request.OrderCreateRequest;
import com.bookstore.shop.readme.dto.request.AdminOrderBulkStatusRequest;
import com.bookstore.shop.readme.dto.response.OrderCreateResponse;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderListResponse;
import com.bookstore.shop.readme.dto.response.OrderSummaryResponse;
import com.bookstore.shop.readme.repository.CartItemRepository;
import com.bookstore.shop.readme.repository.DeliveryRepository;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.OrderItemRepository;
import com.bookstore.shop.readme.repository.OrderRepository;
import com.bookstore.shop.readme.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String ADMIN_CANCEL_REASON = "관리자 주문 취소";
    private static final String ADMIN_OUT_OF_STOCK_BULK_CANCEL_REASON = "관리자 품절 일괄 취소";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    private static final int EXCHANGE_FEE = 3000;
    private static final int RETURN_FEE = 6000;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        List<com.bookstore.shop.readme.domain.CartItem> cartItems =
                cartItemRepository.findAllById(request.getCartItemIds());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("주문할 상품이 없습니다.");
        }

        int totalPrice = cartItems.stream()
                .mapToInt(item -> item.getProduct().getSalePrice() * item.getQuantity())
                .sum();
        int discountAmount = 0;
        int finalPrice = totalPrice - discountAmount;

        Order order = Order.builder()
                .member(member)
                .number(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .orderStatus(OrderStatus.PAYMENT_PENDING) // 결제 완료 전 초기 상태 (30분 초과 시 자동 취소)
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

        for (com.bookstore.shop.readme.domain.CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int itemTotal = product.getSalePrice() * cartItem.getQuantity();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productTitle(product.getTitle())
                    .productAuthor(product.getAuthor())
                    .salePrice(product.getSalePrice())
                    .quantity(cartItem.getQuantity())
                    .itemTotal(itemTotal)
                    .build();
            orderItemRepository.save(orderItem);
        }

        String itemName = cartItems.get(0).getProduct().getTitle()
                + (cartItems.size() > 1 ? " 외 " + (cartItems.size() - 1) + "건" : "");

        return new OrderCreateResponse(order.getId(), finalPrice, itemName);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrderList(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable)
                .map(OrderSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, Long memberId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return new OrderDetailResponse(order, items, payment);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(Long memberId, Pageable pageable) {
        return ResponseEntity.ok(buildOrderListResponsePage(orderRepository.findByMemberId(memberId, pageable)));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return ResponseEntity.ok(new OrderDetailResponse(order, items, payment));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(buildOrderListResponsePage(orderRepository.findAll(pageable)));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return ResponseEntity.ok(buildOrderListResponsePage(orderRepository.findAllByOrderStatus(status, pageable)));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return ResponseEntity.ok(new OrderDetailResponse(order, items, payment));
    }

    @Transactional
    public ResponseEntity<String> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        OrderStatus nextStatus = parseOrderStatus(status);
        if (nextStatus == OrderStatus.CANCELED) {
            cancelOrderInternal(order, ADMIN_CANCEL_REASON);
        } else {
            applyOrderStatusChange(order, nextStatus);
        }

        return ResponseEntity.ok("주문 상태가 변경되었습니다.");
    }

    @Transactional
    public ResponseEntity<String> updateOrderStatuses(AdminOrderBulkStatusRequest request) {
        if (request == null || request.orderIds() == null || request.orderIds().isEmpty()) {
            throw new RuntimeException("상태를 변경할 주문을 선택해 주세요.");
        }

        OrderStatus nextStatus = parseOrderStatus(request.status());
        List<Order> orders = orderRepository.findAllById(request.orderIds());
        if (orders.size() != request.orderIds().size()) {
            throw new RuntimeException("일부 주문을 찾을 수 없습니다.");
        }

        if (nextStatus == OrderStatus.CANCELED) {
            validateOutOfStockOrdersForBulkCancel(orders);
            for (Order order : orders) {
                cancelOrderInternal(order, ADMIN_OUT_OF_STOCK_BULK_CANCEL_REASON);
            }
            return ResponseEntity.ok(orders.size() + "건의 주문이 품절 취소되었습니다.");
        }

        for (Order order : orders) {
            applyOrderStatusChange(order, nextStatus);
        }

        return ResponseEntity.ok(orders.size() + "건의 주문 상태가 변경되었습니다.");
    }

    @Transactional
    public void cancelOrder(Long orderId, Long memberId, String cancelReason) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        cancelOrderInternal(order, cancelReason);
    }

    private void cancelOrderInternal(Order order, String cancelReason) {
        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new RuntimeException("이미 취소된 주문입니다.");
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("배송 완료된 주문은 주문 취소가 불가능합니다.");
        }

        boolean shippingStarted = hasShippingStarted(order.getId(), order.getOrderStatus());
        int returnFee = shippingStarted ? RETURN_FEE : 0;

        paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.PAID || payment.getPaymentStatus() == PaymentStatus.READY) {
                int refundAmount = Math.max(payment.getAmount() - returnFee, 0);
                String detailedCancelReason = buildCancelReason(cancelReason, returnFee, refundAmount, payment.getAmount());
                paymentService.cancelPayment(order.getId(), detailedCancelReason, refundAmount);
            }
        });

        order.setOrderStatus(OrderStatus.CANCELED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new RuntimeException("지원하지 않는 주문 상태입니다. " + status);
        }
    }

    /**
     * 관리자 주문 상태 전환 규칙
     *
     * PAYMENT_PENDING → CANCELED
     * PENDING / PAYED → APPROVAL, CANCELED
     * APPROVAL        → DELIVERING, CANCELED
     * DELIVERING      → DELIVERED
     * DELIVERED       → (변경 불가)
     * CANCELED        → (변경 불가)
     */
    private void validateAdminStatusTransition(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        switch (currentStatus) {
            case PAYMENT_PENDING -> {
                // 결제 미완료 주문: 취소만 가능
                if (nextStatus != OrderStatus.CANCELED) {
                    throw new RuntimeException("결제 진행 중인 주문은 취소만 가능합니다.");
                }
            }
            case PENDING, PAYED -> {
                // 결제 완료 대기: 관리자 승인 또는 취소만 가능
                if (nextStatus != OrderStatus.APPROVAL && nextStatus != OrderStatus.CANCELED) {
                    throw new RuntimeException("결제 완료 주문은 승인(APPROVAL) 또는 취소(CANCELED)만 가능합니다.");
                }
            }
            case APPROVAL -> {
                // 승인 완료: 배송 시작 또는 취소만 가능
                if (nextStatus != OrderStatus.DELIVERING && nextStatus != OrderStatus.CANCELED) {
                    throw new RuntimeException("승인된 주문은 배송 시작(DELIVERING) 또는 취소(CANCELED)만 가능합니다.");
                }
            }
            case DELIVERING -> {
                // 배송 중: 배송 완료만 가능
                if (nextStatus != OrderStatus.DELIVERED) {
                    throw new RuntimeException("배송 중인 주문은 배송 완료(DELIVERED)로만 변경 가능합니다.");
                }
            }
            case DELIVERED -> throw new RuntimeException("이미 배송 완료된 주문은 상태를 변경할 수 없습니다.");
            case CANCELED  -> throw new RuntimeException("이미 취소된 주문은 상태를 변경할 수 없습니다.");
        }
    }

    private void applyOrderStatusChange(Order order, OrderStatus nextStatus) {
        validateAdminStatusTransition(order.getOrderStatus(), nextStatus);

        order.setOrderStatus(nextStatus);
        if (nextStatus == OrderStatus.APPROVAL) {
            ensureReadyDelivery(order);
        }
        if (nextStatus == OrderStatus.CANCELED && order.getCancelledAt() == null) {
            order.setCancelledAt(LocalDateTime.now());
        }
    }

    private void ensureReadyDelivery(Order order) {
        if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setDeliveryStatus(DeliveryStatus.READY);
        deliveryRepository.save(delivery);
    }

    private boolean hasShippingStarted(Long orderId, OrderStatus orderStatus) {
        if (orderStatus == OrderStatus.DELIVERING) {
            return true;
        }

        return deliveryRepository.findByOrderId(orderId)
                .map(delivery -> delivery.getDeliveryStatus() == DeliveryStatus.SHIPPED
                        || delivery.getDeliveryStatus() == DeliveryStatus.IN_TRANSIT
                        || delivery.getDeliveryStatus() == DeliveryStatus.DELIVERED)
                .orElse(false);
    }

    private String buildCancelReason(String baseReason, int returnFee, int refundAmount, int originalAmount) {
        if (returnFee <= 0) {
            return baseReason;
        }

        return String.format(
                "%s (배송 출발 후 취소: 반품비 %,d원 차감, 환불 %,d원 / 교환비 기준 %,d원)",
                baseReason,
                returnFee,
                refundAmount,
                EXCHANGE_FEE
        );
    }

    private void validateOutOfStockOrdersForBulkCancel(List<Order> orders) {
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrderId = buildItemsByOrderId(orderIds);

        boolean hasInvalidOrder = orders.stream()
                .anyMatch(order -> !hasOutOfStockItem(itemsByOrderId.get(order.getId())));

        if (hasInvalidOrder) {
            throw new RuntimeException("재고가 주문 수량보다 부족한 상품이 포함된 주문만 일괄 취소할 수 있습니다.");
        }
    }

    private Page<OrderListResponse> buildOrderListResponsePage(Page<Order> ordersPage) {
        List<Long> orderIds = ordersPage.getContent().stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrderId = buildItemsByOrderId(orderIds);

        return ordersPage.map(order -> new OrderListResponse(
                order,
                buildItemSummary(itemsByOrderId.get(order.getId())),
                hasOutOfStockItem(itemsByOrderId.get(order.getId()))
        ));
    }

    private Map<Long, List<OrderItem>> buildItemsByOrderId(List<Long> orderIds) {
        Map<Long, List<OrderItem>> itemsByOrderId = new HashMap<>();

        if (!orderIds.isEmpty()) {
            for (OrderItem item : orderItemRepository.findByOrderIdIn(orderIds)) {
                itemsByOrderId.computeIfAbsent(item.getOrder().getId(), ignored -> new ArrayList<>()).add(item);
            }
        }

        return itemsByOrderId;
    }

    private String buildItemSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "주문 상품 정보 없음";
        }

        items.sort(Comparator.comparing(OrderItem::getId));
        OrderItem first = items.get(0);
        return items.size() == 1
                ? first.getProductTitle()
                : first.getProductTitle() + " 외 " + (items.size() - 1) + "건";
    }

    private boolean hasOutOfStockItem(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        return items.stream().anyMatch(item ->
                item.getProduct() == null || item.getProduct().getStock() < item.getQuantity()
        );
    }
}
