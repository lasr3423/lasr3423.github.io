package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponse {

    private final Long id;
    private final Long noticeId;
    private final String title;
    private final String content;
    private final boolean isFixed;
    private final boolean pinned;
    private final int viewCount;
    private final String authorName;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public NoticeResponse(Notice notice) {
        this.id = notice.getId();
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.isFixed = notice.isFixed();
        this.pinned = notice.isFixed();
        this.viewCount = notice.getViewCount();
        this.authorName = notice.getAuthor() != null ? notice.getAuthor().getName() : "관리자";
        this.deletedAt = notice.getDeletedAt();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }
}
