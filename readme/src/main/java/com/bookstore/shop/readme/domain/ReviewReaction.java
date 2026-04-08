package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 리뷰 반응(좋아요/싫어요) 엔티티 — DB설계서 review_reaction 테이블
 * 회원 1명당 리뷰 1건에 반응 1개 (UNIQUE: review_id + member_id)
 */
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "review_reaction",
    uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "member_id"})
)
public class ReviewReaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 반응 타입 (LIKE / DISLIKE) — DB설계서 review_reaction.reaction_type */
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 10)
    private ReactionType reactionType;
}
