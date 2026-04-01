package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Long, Product> {

}
