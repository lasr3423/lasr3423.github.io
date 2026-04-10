package com.bookstore.shop.readme.dto.request;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        String title,
        String author,
        String isbn,
        String description,
        int price,
        BigDecimal discountRate,
        int stock,
        String thumbnail,
        String productStatus
) {}
