package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

// 순서를 Entity, ID타입 순으로 교정했습니다.
public interface ProductRepository extends JpaRepository<Product, Long> {
}