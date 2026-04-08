package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

// [신규] 공지사항 엔티티
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notice")
public class Notice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;          // 작성자 (관리자)

    @Column(nullable = false, length = 200)
    private String title;           // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // 내용

    /** 상단 고정 여부 — DB 컬럼명: is_fixed */
    @Column(name = "is_fixed", nullable = false)
    @Builder.Default
    private boolean isFixed = false;

    /** 조회수 — DB 컬럼명: view_count */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt; // soft delete
}
