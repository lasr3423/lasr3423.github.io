package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // [클래스 다이어그램 명칭 준수] deletedAt IS NULL 조건은 쿼리로 처리
    Page<Notice> findAllByOrderByIsFixedDescCreatedAtDesc(Pageable pageable);

    Optional<Notice> findByIdAndDeletedAtIsNull(Long id);
}