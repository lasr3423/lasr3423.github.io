package com.bookstore.shop.readme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaRequest {
    private String title;
    private String content;
    private String category;     // 문의 카테고리 (상품, 배송, 기타 등)
    private Boolean isSecret;    // 비밀글 여부
    private Long productId;      // 상품 관련 문의일 경우 (선택)
}