package com.bookstore.shop.readme.dto.request;

public record NoticeCreateRequest(
        String title,
        String content,
        boolean pinned
) {}
