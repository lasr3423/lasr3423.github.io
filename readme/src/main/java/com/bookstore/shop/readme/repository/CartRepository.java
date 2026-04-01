package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 회원 아이디로 장바구니 조회
    Optional<Cart> findByMemberId(Long memberId);
}
