package com.bookstore.shop.readme.dto;

import com.bookstore.shop.readme.domain.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponseDto {

    private Long id;
    private String title;
    private String content;
    private Boolean isFixed;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // [설계서 준수] 엔티티 직접 노출 금지 — 정적 팩토리 메서드로 변환
    public static NoticeResponseDto from(Notice notice) {
        NoticeResponseDto dto = new NoticeResponseDto();
        dto.id = notice.getId();
        dto.title = notice.getTitle();
        dto.content = notice.getContent();
        dto.isFixed = notice.getIsFixed();
        dto.viewCount = notice.getViewCount();
        dto.createdAt = notice.getCreatedAt();
        dto.updatedAt = notice.getUpdatedAt();
        return dto;
    }
}