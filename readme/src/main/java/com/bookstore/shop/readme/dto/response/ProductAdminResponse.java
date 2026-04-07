package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Product;

import java.math.BigDecimal;

public record ProductAdminResponse(
        Long id,
        String title,
        String author,
        int price,
        BigDecimal discountRate,
        int salePrice,
        int stock,
        int salesCount,
        int viewCount,
        String productStatus,
        String thumbnail
) {
    public ProductAdminResponse(Product p) {
        this(
                p.getId(), p.getTitle(), p.getAuthor(),
                p.getPrice(), p.getDiscountRate(), p.getSalePrice(),
                p.getStock(), p.getSalesCount(), p.getViewCount(),
                p.getProductStatus().name(), p.getThumbnail()
        );
    }
}
