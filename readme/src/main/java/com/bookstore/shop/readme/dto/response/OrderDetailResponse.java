package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String number,
        String orderStatus,
        int totalPrice,
        int discountAmount,
        int finalPrice,
        String receiverName,
        String receiverPhone,
        String deliveryAddress,
        String deliveryAddressDetail,
        String deliveryZipCode,
        String deliveryMemo,
        LocalDateTime orderAt,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(
            Long id,
            String productTitle,
            String productAuthor,
            int salePrice,
            int quantity,
            int itemTotal,
            boolean isReviewed
    ) {
        public OrderItemResponse(OrderItem oi) {
            this(oi.getId(), oi.getProductTitle(), oi.getProductAuthor(),
                    oi.getSalePrice(), oi.getQuantity(), oi.getItemTotal(), oi.isReviewed());
        }
    }

    public OrderDetailResponse(Order o, List<OrderItem> items) {
        this(
                o.getId(), o.getNumber(), o.getOrderStatus().name(),
                o.getTotalPrice(), o.getDiscountAmount(), o.getFinalPrice(),
                o.getReceiverName(), o.getReceiverPhone(),
                o.getDeliveryAddress(), o.getDeliveryAddressDetail(),
                o.getDeliveryZipCode(), o.getDeliveryMemo(), o.getOrderAt(),
                items.stream().map(OrderItemResponse::new).toList()
        );
    }
}
