package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.CartAddRequest;
import com.bookstore.shop.readme.dto.request.CartUpdateRequest;
import com.bookstore.shop.readme.dto.response.CartItemResponse;
import com.bookstore.shop.readme.repository.CartItemRepository;
import com.bookstore.shop.readme.repository.CartRepository;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 내 장바구니 목록 조회
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Long memberId) {

        // 1. 회원 장바구니 조회 - 아무도 없으면 빈 리스트 반환
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElse(null);

        if (cart == null) return List.of();

        // 2. 장바구니에 담긴 상품 목록 조회
        return cartItemRepository.findByCartId(cart.getId())
                .stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());

    }

    // 내 장바구니 상품 추가
    @Transactional
    public void addToCart(CartAddRequest req, Long memberId) {

        // 1. 회원 조회(로그인이 되어있느지 확인)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 2. 상품 조회 - ACTIVATE 상태인 상품만 담을 수 있음
        Product product = productRepository.findByIdAndProductStatus(
                req.getProductId(), ProductStatus.ACTIVATE
        ).orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 3. 재고 확인
        if (product.getStock() < req.getQuantity()) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 4. 장바구니가 없으면 생성 (회원당 1개)
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().member(member).build()
                ));

        // 5. 이미 담긴 상품이면 수량을 추가하고, 없는 상품이면 새로 추가하기
        cartItemRepository.findByCartId(cart.getId())
                .stream()
                .filter(item -> item.getProduct().getId().equals(req.getProductId()))   // product.getId()가 일치하는 항목 찾기
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> {
                            existingItem.setQuantity(existingItem.getQuantity() + req.getQuantity());
                        },
                        () -> cartItemRepository.save(
                                CartItem.builder()
                                        .cart(cart)
                                        .product(product)
                                        .quantity(req.getQuantity())
                                        .isChecked(true)    // 기본값 true
                                        .build()
                        )
                );
    }

    // 장바구니 수량 변경
    @Transactional
    public void updateCartItem(Long cartItemId, CartUpdateRequest request, Long memberId) {
        // 1. CartItem 조회 — memberId로 본인 장바구니 항목인지 간접 확인 - 내 장바구니 아니면 예외 처리
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        // 2. 내 장바구니 항목인지 확인
        if (!item.getCart().getMember().getId().equals(memberId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        // 3. 재고 확인
        if (item.getProduct().getStock() < request.getQuantity()) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 4. 수량 업데이트
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
    }

    // 장바구니 상픔 삭제 - 전체 삭제
    @Transactional
    public void removeCartItem(Long cartItemId, Long memberId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        // 내 장바구니인지 확인
        if(!item.getCart().getMember().getId().equals(memberId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        cartItemRepository.delete(item);
    }

}
