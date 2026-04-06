package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.CartAddRequest;
import com.bookstore.shop.readme.dto.request.CartUpdateRequest;
import com.bookstore.shop.readme.dto.response.CartItemResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    // GET - 내 장바구니 목록 조회
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<CartItemResponse> result = cartService.getCartItems(userDetails.getMemberId());

        return ResponseEntity.ok(result);
    }

    // POST - 장바구니 상품 추가
    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestBody CartAddRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        cartService.addToCart(req, userDetails.getMemberId());

        return ResponseEntity.ok("장바구니에 상품이 담겼습니다.");
    }

    // PUT - 장바구니 수량 변경
    @PutMapping("/{cartItemId}")
    public ResponseEntity<String> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody CartUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        cartService.updateCartItem(cartItemId, req, userDetails.getMemberId());
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }

    // DELETE - 장바구니 상품 전체 삭제
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<String> removeCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        cartService.removeCartItem(cartItemId, userDetails.getMemberId());
        return ResponseEntity.ok("삭제되었습니다.");
    }


}
