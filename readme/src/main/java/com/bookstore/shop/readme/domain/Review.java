package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity // [수정] 추가
@Getter
@Setter
@NoArgsConstructor // [보완] JPA 필수
@Table(name = "review")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // [보완] 설계서 필수값
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // [보완] 설계서 필수값
    private Product product;

    @Column(nullable = false) // [보완] 설계서 필수값
    private Integer rating;

    @Column(columnDefinition = "TEXT", nullable = false) // [보완] 설계서 TEXT 타입
    private String content;

    @Column(nullable = false) // [보완] 설계서 기본값 0
    private Integer hits = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();
}