package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndProductStatus(Long id, ProductStatus status);

    // 전체 조회
    Page<Product> findAllByProductStatus(ProductStatus status, Pageable pageable);

    // 대분류 필터
    Page<Product> findAllByProductStatusAndCategoryTopId(ProductStatus status, Long categoryTopId, Pageable pageable);

    // 소분류 필터
    Page<Product> findAllByProductStatusAndCategoryTopIdAndCategorySubId(ProductStatus status, Long categoryTopId, Long categorySubId, Pageable pageable);
}
