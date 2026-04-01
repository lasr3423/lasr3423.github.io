package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category_top")
public class CategoryTop extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;    // 카테고리 대분류 명

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;  // 화면 정렬 순서

    @Enumerated(EnumType.STRING)
    private CategoryStatus categoryStatus;  // 카테고리 상태
}
