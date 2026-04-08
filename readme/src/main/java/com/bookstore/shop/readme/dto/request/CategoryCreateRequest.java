package com.bookstore.shop.readme.dto.request;

// 카테고리 등록 요청 DTO — FA-027/FA-028
public record CategoryCreateRequest(
        String name,            // 카테고리명
        Integer sortOrder,      // 정렬 순서 (기본 0)
        Long categoryTopId      // 소분류 등록 시에만 사용 (대분류 등록 시 null)
) {}
