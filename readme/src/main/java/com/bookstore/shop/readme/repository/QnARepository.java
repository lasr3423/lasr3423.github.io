package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.QnA;
import com.bookstore.shop.readme.domain.QnaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * QnA Repository — DB설계서 트리 구조(parent_id / depth) 기반
 *
 * 최상위 질문(depth=0): parent IS NULL
 * 답변/재문의(depth>=1): parent IS NOT NULL
 */
public interface QnARepository extends JpaRepository<QnA, Long> {

    // ── 최상위 질문(depth=0) 목록 ─────────────────────────────────────────────

    /** 비밀글 제외 공개 질문 목록 (비로그인 사용자용) */
    Page<QnA> findAllByDepthAndIsSecretFalseAndDeletedAtIsNull(int depth, Pageable pageable);

    /** 특정 회원의 질문 목록 (본인 비밀글 포함) */
    Page<QnA> findAllByDepthAndMemberIdAndDeletedAtIsNull(int depth, Long memberId, Pageable pageable);

    /** 관리자 전체 질문 목록 (depth=0) */
    Page<QnA> findAllByDepthAndDeletedAtIsNull(int depth, Pageable pageable);

    /** 관리자 — 특정 상태의 질문 목록 (예: WAITING) */
    Page<QnA> findAllByDepthAndQnaStatusAndDeletedAtIsNull(int depth, QnaStatus qnaStatus, Pageable pageable);

    // ── 자식 QnA (답변 / 재문의) ──────────────────────────────────────────────

    /** 특정 QnA의 직접 자식 목록 (삭제 제외) */
    List<QnA> findAllByParentIdAndDeletedAtIsNull(Long parentId);

    // ── 통계 ────────────────────────────────────────────────────────────────

    /** 대시보드 — 미답변 질문 수 (depth=0, WAITING 상태) */
    long countByDepthAndQnaStatusAndDeletedAtIsNull(int depth, QnaStatus qnaStatus);

    // ── 복합 쿼리 ───────────────────────────────────────────────────────────

    /**
     * 특정 회원이 작성한 depth=0 질문 단건 조회
     * (마이페이지 상세에서 본인 소유 확인용)
     */
    @Query("SELECT q FROM QnA q WHERE q.id = :id AND q.member.id = :memberId AND q.deletedAt IS NULL")
    java.util.Optional<QnA> findByIdAndMemberIdAndDeletedAtIsNull(
            @Param("id") Long id,
            @Param("memberId") Long memberId);

    /**
     * 관리자 — 카테고리별 질문 필터
     */
    @Query("SELECT q FROM QnA q WHERE q.depth = 0 AND q.category = :category AND q.deletedAt IS NULL")
    Page<QnA> findAllByDepthZeroAndCategory(
            @Param("category") String category,
            Pageable pageable);
}
