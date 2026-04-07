package com.bookstore.shop.readme.dto.request;

public record QnACreateRequest(
        Long productId,   // null 이면 일반 문의
        String title,
        String content,
        boolean secret
) {}
