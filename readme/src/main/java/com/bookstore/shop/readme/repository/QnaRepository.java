package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // 추가

public interface QnaRepository extends JpaRepository<Qna, Long> {

    /**
     * [추가] 상세 조회 시 Soft Delete(삭제일) 체크
     * 서비스 레이어의 getQnaDetail에서 사용하기 위해 필수입니다.
     */
    Optional<Qna> findByIdAndDeletedAtIsNull(Long id);

    /**
     * [설계서 v1.3 인덱스 반영]
     * 사용자의 문의 내역 중 질문(depth=0)만 최신순 조회
     */
    Page<Qna> findByMemberIdAndDepthAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId, Integer depth, Pageable pageable);

    /**
     * [설계서 idx_qna_status 활용] 관리자용: 미답변 내역 우선 조회
     */
    Page<Qna> findByDeletedAtIsNullOrderByQnaStatusAscCreatedAtDesc(Pageable pageable);
}