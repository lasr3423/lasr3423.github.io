package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    // 설계서: 내 문의 내역 조회 (질문 원문만)
    Page<Qna> findByMemberIdAndDepthAndDeletedAtIsNull(Long memberId, Integer depth, Pageable pageable);
}