package com.bookstore.shop.readme.dto.request;

import java.util.List;

// 리뷰 수정 요청 DTO — REQ-M-018
public record ReviewUpdateRequest(
        int rating,                // 1~5
        String content,
        List<String> imageUrls     // null이면 기존 이미지 유지 / 빈 리스트면 전체 삭제
) {}
