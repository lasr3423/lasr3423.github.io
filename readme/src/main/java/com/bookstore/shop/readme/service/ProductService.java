package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.ProductStatus;
import com.bookstore.shop.readme.dto.response.ProductDetailResponse;
import com.bookstore.shop.readme.dto.response.ProductListResponse;
import com.bookstore.shop.readme.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 전체 목록 조회
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProductList(Pageable pageable) {
        return productRepository
                // ACTIVATE 상태인 상품만 조회(DEACTIVATE, DELETE 제외)
                .findAllByProductStatus(ProductStatus.ACTIVATE, pageable)
                .map(ProductListResponse::new);
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        // ACTIVATE 상태인 상품만 허용 - 삭제된 상품은 직접 URL 접근 방지!!
        Product product = productRepository
                .findByIdAndProductStatus(productId, ProductStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 지금은 단순 조회수 +1 하는 코드 추후 수정 필요!!
        product.setViewCount(product.getViewCount()+1);
        productRepository.save(product);

        return new ProductDetailResponse(product);
    }

    //
}
