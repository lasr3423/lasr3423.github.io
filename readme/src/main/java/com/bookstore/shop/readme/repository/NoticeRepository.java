package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 삭제되지 않은 공지사항만 조회 (soft delete)
    Page<Notice> findAllByDeletedAtIsNull(Pageable pageable);

    // 상단 고정 공지사항
    java.util.List<Notice> findAllByPinnedTrueAndDeletedAtIsNull();
}
