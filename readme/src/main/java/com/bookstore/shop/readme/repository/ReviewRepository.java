package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * [설계서 v1.3 제약조건]
     * 상세 조회 시 삭제되지 않은(deletedAt IS NULL) 데이터만 조회
     */
    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    /**
     * [설계서 인덱스 전략 준수]
     * 특정 상품의 리뷰를 삭제되지 않은 건에 한해 최신순(CreatedAt DESC)으로 페이징 조회
     */
    Page<Review> findByProductIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long productId, Pageable pageable);

    /**
     * [설계서 v1.3 UNIQUE 제약 대응]
     * 동일 회원이 동일 상품에 대해 중복 리뷰를 남기는지 체크 (Soft Delete된 리뷰는 제외)
     */
    boolean existsByMemberIdAndProductIdAndDeletedAtIsNull(Long memberId, Long productId);
}