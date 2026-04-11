package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Order;

import java.time.LocalDateTime;

public record OrderListResponse(
        Long orderId,
        String number,
        String memberName,
        String itemSummary,
        String orderStatus,
        int finalPrice,
        LocalDateTime orderAt
) {
    public OrderListResponse(Order o) {
        this(
                o.getId(),
                o.getNumber(),
                o.getMember() != null ? o.getMember().getName() : null,
                null,
                o.getOrderStatus().name(),
                o.getFinalPrice(),
                o.getOrderAt()
        );
    }

    public OrderListResponse(Order o, String itemSummary) {
        this(
                o.getId(),
                o.getNumber(),
                o.getMember() != null ? o.getMember().getName() : null,
                itemSummary,
                o.getOrderStatus().name(),
                o.getFinalPrice(),
                o.getOrderAt()
        );
    }
}
