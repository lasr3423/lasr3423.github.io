package com.bookstore.shop.readme.dto.request;

public record NoticeCreateRequest(
        String title,
        String content,
        boolean isFixed   // DB설계서 notice.is_fixed (상단 고정 여부)
) {}
