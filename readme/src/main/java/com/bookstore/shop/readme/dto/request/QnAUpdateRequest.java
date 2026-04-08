package com.bookstore.shop.readme.dto.request;

// QnA 수정 요청 DTO — REQ-M-023
public record QnAUpdateRequest(
        String title,
        String content
) {}
