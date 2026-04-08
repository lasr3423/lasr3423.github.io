package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Delivery;
import com.bookstore.shop.readme.domain.DeliveryStatus;
import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderItem;
import com.bookstore.shop.readme.domain.OrderStatus;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.dto.request.OrderCreateRequest;
import com.bookstore.shop.readme.dto.response.OrderCreateResponse;
import com.bookstore.shop.readme.dto.response.OrderDetailResponse;
import com.bookstore.shop.readme.dto.response.OrderListResponse;
import com.bookstore.shop.readme.dto.response.OrderSummaryResponse;
import com.bookstore.shop.readme.repository.CartItemRepository;
import com.bookstore.shop.readme.repository.DeliveryRepository;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.OrderItemRepository;
import com.bookstore.shop.readme.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentService paymentService;

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

        cartItemRepository.deleteAll(cartItems);

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
        return new OrderDetailResponse(order, items);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(Long memberId, Pageable pageable) {
        return ResponseEntity.ok(
                orderRepository.findByMemberId(memberId, pageable).map(OrderListResponse::new)
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderRepository.findAll(pageable).map(OrderListResponse::new));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailResponse> getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    @Transactional
    public ResponseEntity<String> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        OrderStatus nextStatus = parseOrderStatus(status);
        validateAdminStatusTransition(order.getOrderStatus(), nextStatus);

        order.setOrderStatus(nextStatus);
        if (nextStatus == OrderStatus.APPROVAL) {
            ensureReadyDelivery(order);
        }
        if (nextStatus == OrderStatus.CANCELED && order.getCancelledAt() == null) {
            order.setCancelledAt(LocalDateTime.now());
        }

        return ResponseEntity.ok("주문 상태가 변경되었습니다.");
    }

    @Transactional
    public void cancelOrder(Long orderId, Long memberId, String cancelReason) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new RuntimeException("이미 취소된 주문입니다.");
        }

        if (order.getOrderStatus() == OrderStatus.PAYED) {
            paymentService.cancelPayment(orderId, cancelReason);
        }

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

    private void validateAdminStatusTransition(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        switch (currentStatus) {
            case PENDING -> {
                if (nextStatus != OrderStatus.CANCELED) {
                    throw new RuntimeException("결제 전 주문은 취소만 가능합니다.");
                }
            }
            case PAYED -> {
                if (nextStatus != OrderStatus.APPROVAL && nextStatus != OrderStatus.CANCELED) {
                    throw new RuntimeException("결제 완료 주문은 승인 또는 취소만 가능합니다.");
                }
            }
            case APPROVAL -> throw new RuntimeException("이미 승인된 주문은 상태를 되돌릴 수 없습니다.");
            case CANCELED -> throw new RuntimeException("이미 취소된 주문은 상태를 변경할 수 없습니다.");
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
}
