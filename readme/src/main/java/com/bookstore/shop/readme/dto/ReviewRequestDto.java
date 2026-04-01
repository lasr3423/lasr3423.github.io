package com.bookstore.shop.readme.dto;

public record ReviewRequestDto(
        Long productId,
        Integer rating,
        String content
) {}