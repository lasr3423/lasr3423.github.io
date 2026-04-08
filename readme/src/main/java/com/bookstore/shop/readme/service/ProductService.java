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

    // 상품 목록 조회 + 키워드 검색 (REQ-P-001, REQ-P-003, REQ-P-004 통합)
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProductList(Long categoryTopId, Long categorySubId,
                                                     String keyword, Pageable pageable) {
        // 키워드 검색이 있는 경우
        if (keyword != null && !keyword.isBlank()) {
            if (categoryTopId != null && categorySubId != null) {
                return productRepository
                        .searchByKeywordAndSub(ProductStatus.ACTIVATE, categoryTopId, categorySubId, keyword, pageable)
                        .map(ProductListResponse::new);
            }
            if (categoryTopId != null) {
                return productRepository
                        .searchByKeywordAndTop(ProductStatus.ACTIVATE, categoryTopId, keyword, pageable)
                        .map(ProductListResponse::new);
            }
            return productRepository
                    .searchByKeyword(ProductStatus.ACTIVATE, keyword, pageable)
                    .map(ProductListResponse::new);
        }

        // 키워드 없는 일반 조회
        if (categoryTopId != null && categorySubId != null) {
            return productRepository
                    .findAllByProductStatusAndCategoryTopIdAndCategorySubId(
                            ProductStatus.ACTIVATE, categoryTopId, categorySubId, pageable)
                    .map(ProductListResponse::new);
        }
        if (categoryTopId != null) {
            return productRepository
                    .findAllByProductStatusAndCategoryTopId(
                            ProductStatus.ACTIVATE, categoryTopId, pageable)
                    .map(ProductListResponse::new);
        }
        return productRepository
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
