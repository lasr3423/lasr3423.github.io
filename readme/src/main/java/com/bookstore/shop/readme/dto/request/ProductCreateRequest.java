package com.bookstore.shop.readme.dto.request;

import java.math.BigDecimal;

public record ProductCreateRequest(
        Long categoryTopId,
        Long categorySubId,
        String title,
        String author,
        String description,
        int price,
        BigDecimal discountRate,
        int stock,
        String thumbnail
) {}
