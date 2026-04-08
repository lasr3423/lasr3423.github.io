package com.bookstore.shop.readme.dto.request;

import java.util.List;

public record ReviewCreateRequest(
        Long productId,
        int rating,                // 1~5
        String content,
        List<String> imageUrls     // 리뷰 이미지 URL 목록 (최대 5장, 선택)
) {}
