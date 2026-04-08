package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

// [신규] 상품 리뷰 엔티티
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;          // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;        // 리뷰 대상 상품

    @Column(nullable = false)
    private int rating;             // 별점 1~5

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // 리뷰 내용

    /** 조회수 — DB설계서 review.hits */
    @Column(name = "hits", nullable = false)
    @Builder.Default
    private int hits = 0;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt; // soft delete
}
