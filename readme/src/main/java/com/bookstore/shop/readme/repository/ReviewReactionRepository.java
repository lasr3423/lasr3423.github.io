package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.ReactionType;
import com.bookstore.shop.readme.domain.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {

    /** 특정 회원의 특정 리뷰에 대한 반응 조회 */
    Optional<ReviewReaction> findByReviewIdAndMemberId(Long reviewId, Long memberId);

    /** 리뷰별 특정 타입 반응 수 (좋아요 수, 싫어요 수) */
    long countByReviewIdAndReactionType(Long reviewId, ReactionType reactionType);
}
