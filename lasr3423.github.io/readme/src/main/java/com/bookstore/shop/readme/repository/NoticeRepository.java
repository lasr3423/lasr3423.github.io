package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * [설계서 제약조건 반영]
     * 1. 상단 고정(isFixed)된 게시글이 먼저 오도록 정렬
     * 2. 동일 조건 내에서는 최신순(createdAt) 정렬
     * 3. 삭제되지 않은(deletedAt IS NULL) 게시글만 조회
     */
    Page<Notice> findAllByDeletedAtIsNullOrderByIsFixedDescCreatedAtDesc(Pageable pageable);

    /**
     * [소프트 삭제 제약조건]
     * 상세 조회 시에도 삭제되지 않은 데이터만 조회해야 함
     */
    Optional<Notice> findByIdAndDeletedAtIsNull(Long id);
}