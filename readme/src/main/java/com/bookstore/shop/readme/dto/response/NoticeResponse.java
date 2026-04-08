package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final boolean isFixed;    // DB설계서 notice.is_fixed
    private final int viewCount;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public NoticeResponse(Notice notice) {
        this.id         = notice.getId();
        this.title      = notice.getTitle();
        this.content    = notice.getContent();
        this.isFixed    = notice.isFixed();
        this.viewCount  = notice.getViewCount();
        this.authorName = notice.getAuthor() != null ? notice.getAuthor().getName() : "관리자";
        this.createdAt  = notice.getCreatedAt();
        this.updatedAt  = notice.getUpdatedAt();
    }
}
