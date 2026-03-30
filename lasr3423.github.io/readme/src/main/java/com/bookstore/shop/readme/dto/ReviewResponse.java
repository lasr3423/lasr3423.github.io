package com.bookstore.shop.readme.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReviewResponse {
    private Long id;
    private String writerName;   // 작성자 이름 (Member에서 추출)
    private String content;
    private Integer rating;
    private Integer hits;        // 조회수
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}