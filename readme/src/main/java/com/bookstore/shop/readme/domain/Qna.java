package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity // [수정] 필수 어노테이션 추가
@Getter
@Setter
@NoArgsConstructor // [보완] JPA 필수 기본 생성자
@Table(name = "qna")
public class Qna extends BaseEntity {

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false) // [보완] 설계서 ❌ NULL 허용 반영
    private Integer depth = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // [보완] 설계서 FK 제약조건 반영
    private Member member;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "qna_status", nullable = false)
    private QnaStatus qnaStatus = QnaStatus.WAITING;

    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret = false;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}