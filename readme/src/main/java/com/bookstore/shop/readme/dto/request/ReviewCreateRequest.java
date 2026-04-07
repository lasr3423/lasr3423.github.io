package com.bookstore.shop.readme.dto.request;

public record ReviewCreateRequest(
        Long productId,
        int rating,     // 1~5
        String content
) {}
