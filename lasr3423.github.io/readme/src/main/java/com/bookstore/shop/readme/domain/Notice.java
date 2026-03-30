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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_fixed")
    private Boolean isFixed = false;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}