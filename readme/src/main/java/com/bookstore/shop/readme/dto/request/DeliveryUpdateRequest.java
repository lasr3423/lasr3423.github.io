package com.bookstore.shop.readme.dto.request;

public record DeliveryUpdateRequest(
        String courier,
        String trackingNumber,
        String deliveryStatus
) {}
