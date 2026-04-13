package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.CartItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartItemResponse {

    private final Long cartItemId;
    private final Long productId;
    private final String title;
    private final String author;
    private final int salePrice;
    private final String thumbnail;
    private final int quantity;
    // [수정] boolean isXxx 필드는 Jackson이 getXxx() 게터를 찾아 "checked"로 직렬화함
    //        프론트가 item.isChecked 로 접근하므로 @JsonProperty로 키 명시
    @JsonProperty("isChecked")
    private final boolean isChecked;
    private final int itemTotal;

    // entity -> dto 변환 생성자
    public CartItemResponse(CartItem item) {
        this.cartItemId = item.getId();
        this.productId = item.getProduct().getId();
        this.title = item.getProduct().getTitle();
        this.author = item.getProduct().getAuthor();
        this.salePrice = normalizeSalePrice(item);
        this.thumbnail = item.getProduct().getThumbnail();
        this.quantity = item.getQuantity();
        this.isChecked = item.isChecked();
        this.itemTotal = this.salePrice * item.getQuantity();
    }

    private int normalizeSalePrice(CartItem item) {
        int salePrice = item.getProduct().getSalePrice();
        if (salePrice >= 0) {
            return salePrice;
        }
        int price = item.getProduct().getPrice();
        if (price <= 0) {
            return 0;
        }
        BigDecimal rate = item.getProduct().getDiscountRate() == null
                ? BigDecimal.ZERO
                : item.getProduct().getDiscountRate();
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        BigDecimal ratio = BigDecimal.ONE.subtract(rate.divide(BigDecimal.valueOf(100)));
        return Math.max(0, ratio.multiply(BigDecimal.valueOf(price)).intValue());
    }
}
