package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@Table(name = "qna")
public class Qna extends BaseEntity {
    @Column(name = "parent_id")
    private Long parentId;

    private Integer depth = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String category;
    private String title;
    private String content;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "qna_status")
    private QnaStatus qnaStatus = QnaStatus.WAITING;

    @Column(name = "is_secret")
    private Boolean isSecret = false;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}