package com.bookstore.shop.readme.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponseDto(
        Long id,
        String content,
        Integer rating,
        String memberName,
        String productTitle,
        List<String> imageUrls,
        Integer hits,
        LocalDateTime createdAt
) {}