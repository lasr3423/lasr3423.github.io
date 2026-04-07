package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderItem;
import com.bookstore.shop.readme.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDetailResponse {

    private final Long orderId;
    private final String number;
    private final int totalPrice;
    private final int discountAmount;
    private final int finalPrice;
    private final OrderStatus orderStatus;
    private final String receiverName;
    private final String receiverPhone;
    private final String deliveryAddress;
    private final String deliveryAddressDetail;
    private final String deliveryZipCode;
    private final String deliveryMemo;
    private final LocalDateTime orderAt;
    private final LocalDateTime cancelledAt;     // 취소 시각 (취소 전: null)

    // 주문 상품 목록 — 내부 DTO로 반환
    private final List<OrderItemDto> orderItems;

    // 엔티티 + 주문항목 목록 받아서 변환
    public OrderDetailResponse(Order order, List<OrderItem> items) {
        this.orderId = order.getId();
        this.number = order.getNumber();
        this.totalPrice = order.getTotalPrice();
        this.discountAmount = order.getDiscountAmount();
        this.finalPrice = order.getFinalPrice();
        this.orderStatus = order.getOrderStatus();
        this.receiverName = order.getReceiverName();
        this.receiverPhone = order.getReceiverPhone();
        this.deliveryAddress = order.getDeliveryAddress();
        this.deliveryAddressDetail = order.getDeliveryAddressDetail();
        this.deliveryZipCode = order.getDeliveryZipCode();
        this.deliveryMemo = order.getDeliveryMemo();
        this.orderAt = order.getOrderAt();
        this.cancelledAt = order.getCancelledAt();
        // List<OrderItem> → List<OrderItemDto> 변환
        this.orderItems = items.stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }

    // ── 주문 상품 1건 내부 DTO ───────────────────────────────────────────
    @Getter
    public static class OrderItemDto {
        private final Long orderItemId;
        // ⚠️ OrderItem 엔티티의 상품명 필드는 "productTitle" — "productName" 아님
        private final String productTitle;
        // ⚠️ OrderItem 엔티티의 저자 필드는 "productAuthor"
        private final String productAuthor;
        // ⚠️ OrderItem 엔티티의 단가 필드는 "salePrice" — "price" 아님
        private final int salePrice;
        private final int quantity;
        // ⚠️ OrderItem 엔티티의 소계 필드는 "itemTotal"
        private final int itemTotal;

        public OrderItemDto(OrderItem item) {
            this.orderItemId = item.getId();
            this.productTitle = item.getProductTitle();
            this.productAuthor = item.getProductAuthor();
            this.salePrice = item.getSalePrice();
            this.quantity = item.getQuantity();
            this.itemTotal = item.getItemTotal();
        }
    }
}