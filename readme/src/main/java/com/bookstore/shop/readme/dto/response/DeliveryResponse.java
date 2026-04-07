package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Delivery;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        Long orderId,
        String courier,
        String trackingNumber,
        String deliveryStatus,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {
    public DeliveryResponse(Delivery d) {
        this(
                d.getId(),
                d.getOrder().getId(),
                d.getCourier(),
                d.getTrackingNumber(),
                d.getDeliveryStatus().name(),
                d.getShippedAt(),
                d.getDeliveredAt()
        );
    }
}
