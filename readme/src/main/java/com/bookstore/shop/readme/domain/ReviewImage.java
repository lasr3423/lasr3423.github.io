package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // [수정] 추가
@Getter
@Setter
@NoArgsConstructor // [보완] JPA 필수
@Table(name = "review_image")
public class ReviewImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false) // [보완] 필수값
    private Review review;

    @Column(name = "image_url", length = 512, nullable = false) // [보완] 설계서 길이 512 및 필수값
    private String imageUrl;
}