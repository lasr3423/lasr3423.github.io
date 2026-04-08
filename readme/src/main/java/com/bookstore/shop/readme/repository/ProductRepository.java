package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndProductStatus(Long id, ProductStatus status);

    // 전체 조회
    Page<Product> findAllByProductStatus(ProductStatus status, Pageable pageable);

    // 대분류 필터
    Page<Product> findAllByProductStatusAndCategoryTopId(ProductStatus status, Long categoryTopId, Pageable pageable);

    // 소분류 필터
    Page<Product> findAllByProductStatusAndCategoryTopIdAndCategorySubId(ProductStatus status, Long categoryTopId, Long categorySubId, Pageable pageable);

    // 키워드 검색 (제목 / 저자) — REQ-P-003
    @Query("SELECT p FROM Product p WHERE p.productStatus = :status " +
           "AND (p.title LIKE %:keyword% OR p.author LIKE %:keyword%)")
    Page<Product> searchByKeyword(@Param("status") ProductStatus status,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    // 키워드 + 대분류 검색
    @Query("SELECT p FROM Product p WHERE p.productStatus = :status " +
           "AND p.categoryTop.id = :topId " +
           "AND (p.title LIKE %:keyword% OR p.author LIKE %:keyword%)")
    Page<Product> searchByKeywordAndTop(@Param("status") ProductStatus status,
                                        @Param("topId") Long topId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    // 키워드 + 소분류 검색
    @Query("SELECT p FROM Product p WHERE p.productStatus = :status " +
           "AND p.categoryTop.id = :topId AND p.categorySub.id = :subId " +
           "AND (p.title LIKE %:keyword% OR p.author LIKE %:keyword%)")
    Page<Product> searchByKeywordAndSub(@Param("status") ProductStatus status,
                                        @Param("topId") Long topId,
                                        @Param("subId") Long subId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    // 관리자 대시보드 — 재고 부족 상품 수 (stock < threshold)
    long countByStockLessThanAndProductStatus(int threshold, ProductStatus status);
}
