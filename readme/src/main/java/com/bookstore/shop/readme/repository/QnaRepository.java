package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Qna;
import com.bookstore.shop.readme.domain.QnaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 삭제되지 않은 최상위 질문 목록 (전체, 관리자용)
    Page<Qna> findByParentIsNullAndDeletedAtIsNull(Pageable pageable);

    // 상태 필터 (관리자용)
    Page<Qna> findByParentIsNullAndDeletedAtIsNullAndQnaStatus(QnaStatus qnaStatus, Pageable pageable);

    // 특정 회원 질문 목록 (유저용 마이페이지)
    Page<Qna> findByMemberIdAndParentIsNullAndDeletedAtIsNull(Long memberId, Pageable pageable);

    // 자식(답변) 조회
    List<Qna> findByParentIdAndDeletedAtIsNull(Long parentId);

    // 단건 조회 (삭제 제외)
    Optional<Qna> findByIdAndDeletedAtIsNull(Long id);
}