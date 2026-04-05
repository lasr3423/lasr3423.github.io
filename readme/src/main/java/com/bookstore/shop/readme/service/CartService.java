package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Cart;
import com.bookstore.shop.readme.dto.response.CartItemResponse;
import com.bookstore.shop.readme.repository.CartRepository;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemResponse cartItemResponse;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 내 장바구니 목록 조회
//    @Transactional(readOnly = true)
//    public List<CartItemResponse> getCartItems(Long memberId) {
//
//        // 1. 회원 장바구니 조회 - 아무도 없으면 빈 리스트 반환
//        Optional<Cart> optionalCart = cartRepository.findByMemberId(memberId);
//
//        if (optionalCart.isEmpty()) {
//
//            return Collections.emptyList();
//        }
//
//
//    }
}
