package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_image")
public class ProductImage extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    @ColumnDefault("'SUB'")
    private String type = "SUB";

    @Column(nullable = false)
    @ColumnDefault("0")
    private int sortOrder = 0;
}
