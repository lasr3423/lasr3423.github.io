package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 리뷰 이미지 엔티티 — DB설계서 review_image 테이블
 * 리뷰 1건당 최대 5장
 */
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review_image")
public class ReviewImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** 이미지 URL — DB설계서 review_image.image_url */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
}
