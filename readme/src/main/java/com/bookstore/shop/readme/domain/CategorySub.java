package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category_sub")
public class CategorySub extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_top_id")
    private CategoryTop categoryTop;

    @Column(nullable = false, length = 50)
    private String name;    // 카테고리 소분류 명

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;  // 화면 정렬 순서

    @Enumerated(EnumType.STRING)
    private CategoryStatus categoryStatus;  // 카테고리 상태
}
