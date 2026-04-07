package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Order;

import java.time.LocalDateTime;

public record OrderListResponse(
        Long id,
        String number,
        String orderStatus,
        int finalPrice,
        LocalDateTime orderAt
) {
    public OrderListResponse(Order o) {
        this(
                o.getId(),
                o.getNumber(),
                o.getOrderStatus().name(),
                o.getFinalPrice(),
                o.getOrderAt()
        );
    }
}
