package com.bookstore.shop.readme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity @Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTop extends BaseEntity {

    @Column
    private String name;    // 카테고리 대분류 명
    private Integer sortOrder;  // 화면 정렬 순서
    private CategoryStatus categoryStatus;  // 카테고리 상태
}
