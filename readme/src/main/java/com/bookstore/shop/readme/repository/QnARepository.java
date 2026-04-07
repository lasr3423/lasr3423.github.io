package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.QnA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnARepository extends JpaRepository<QnA, Long> {

    // 특정 상품 QnA 목록 (비밀글 제외 공개 목록)
    Page<QnA> findAllByProductIdAndSecretFalseAndDeletedAtIsNull(Long productId, Pageable pageable);

    // 특정 회원의 QnA 목록 (본인 것은 비밀글도 포함)
    Page<QnA> findAllByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    // 관리자 전체 QnA (미답변 필터 포함)
    Page<QnA> findAllByDeletedAtIsNull(Pageable pageable);
    Page<QnA> findAllByAnswerIsNullAndDeletedAtIsNull(Pageable pageable);
}
