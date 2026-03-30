package com.bookstore.shop.readme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequest {
    // 설계서 1-1 필드 반영
    private String title;
    private String content;
    private Boolean isFixed; // 상단 고정 여부 (기본값 false 처리 권장)
}