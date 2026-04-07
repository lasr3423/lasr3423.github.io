package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.response.ProductDetailResponse;
import com.bookstore.shop.readme.dto.response.ProductListResponse;
import com.bookstore.shop.readme.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    // 유저 페이지

    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> getProductList(
            @RequestParam(required = false) Long categoryTopId,
            @RequestParam(required = false) Long categorySubId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductListResponse> result = productService.getProductList(categoryTopId, categorySubId, pageable);

        return ResponseEntity.ok(result);   // HTTP 200 + JSON
    }

    @GetMapping("/detail/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @PathVariable Long productId) {

        ProductDetailResponse result = productService.getProductDetail(productId);

        return ResponseEntity.ok(result);
    }

}
