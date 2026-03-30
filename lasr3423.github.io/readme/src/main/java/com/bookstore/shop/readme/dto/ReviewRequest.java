package com.bookstore.shop.readme.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ReviewRequest {
    private Long productId;      // 상품 ID (필수)
    private String content;      // 리뷰 내용
    private Integer rating;      // 평점 (1~5)
    private List<String> imageUrls; // 이미지 URL 리스트
}