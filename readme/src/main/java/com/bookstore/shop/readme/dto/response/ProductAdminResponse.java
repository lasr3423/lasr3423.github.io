package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Product;

import java.math.BigDecimal;

public record ProductAdminResponse(
        Long id,
        String title,
        String author,
        String isbn,
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
                p.getId(), p.getTitle(), p.getAuthor(), p.getIsbn(),
                p.getPrice(), p.getDiscountRate(), normalizeSalePrice(p),
                p.getStock(), p.getSalesCount(), p.getViewCount(),
                p.getProductStatus().name(), p.getThumbnail()
        );
    }

    private static int normalizeSalePrice(Product product) {
        if (product.getSalePrice() >= 0) {
            return product.getSalePrice();
        }
        if (product.getPrice() <= 0) {
            return 0;
        }
        BigDecimal rate = product.getDiscountRate() == null ? BigDecimal.ZERO : product.getDiscountRate();
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return product.getPrice();
        }
        BigDecimal ratio = BigDecimal.ONE.subtract(rate.divide(BigDecimal.valueOf(100)));
        return Math.max(0, ratio.multiply(BigDecimal.valueOf(product.getPrice())).intValue());
    }
}
