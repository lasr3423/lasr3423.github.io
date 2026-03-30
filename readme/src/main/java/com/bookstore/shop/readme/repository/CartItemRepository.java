package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // 장바구니에 담긴 상품 목록 조회
    List<CartItem> findByCartId(Long CartId);

    // 특정 상품을 장바구니에서 제거
    void deleteByCartIdAndProductId(Long cartId, Long productId);
}
