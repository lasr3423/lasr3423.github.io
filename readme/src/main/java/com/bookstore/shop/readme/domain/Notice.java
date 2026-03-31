package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notice")
public class Notice extends BaseEntity {

    // [보완] nullable = false 추가 (공지사항은 반드시 작성 관리자가 있어야 함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    // [확인] 설계서에 TEXT 타입으로 명시됨 (잘 반영됨)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_fixed", nullable = false)
    private Boolean isFixed = false; // 기본값 false (설계서 일치)

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0; // 기본값 0 (설계서 일치)

    // [설계서 v1.3] Soft Delete용 컬럼
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}