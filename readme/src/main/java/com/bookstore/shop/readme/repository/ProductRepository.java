package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndProductStatus(Long id, ProductStatus status);

    Page<Product> findAllByProductStatus(ProductStatus status, Pageable pageable);
}
