package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.CartItem;
import lombok.Getter;

@Getter
public class CartItemResponse {

    private final Long cartItemId;
    private final Long productId;
    private final String title;
    private final String author;
    private final int salePrice;
    private final String thumbnail;
    private final int quantity;
    private final boolean isChecked;
    private final int itemTotal;

    // entity -> dto 변환 생성자
    public CartItemResponse(CartItem item) {
        this.cartItemId = item.getId();
        this.productId = item.getProduct().getId();
        this.title = item.getProduct().getTitle();
        this.author = item.getProduct().getAuthor();
        this.salePrice = item.getProduct().getSalePrice();
        this.thumbnail = item.getProduct().getThumbnail();
        this.quantity = item.getQuantity();
        this.isChecked = item.isChecked();
        this.itemTotal = item.getProduct().getSalePrice() * item.getQuantity();
    }
}
