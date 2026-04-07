package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

// [신규] 상품 Q&A 엔티티
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "qna")
public class QnA extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;          // 질문 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;        // 관련 상품 (null 이면 일반 문의)

    @Column(nullable = false, length = 200)
    private String title;           // 질문 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // 질문 내용

    @Column(columnDefinition = "TEXT")
    private String answer;          // 관리자 답변 (null = 미답변)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by")
    private Member answeredBy;      // 답변 작성 관리자

    @Column(name = "answered_at")
    private java.time.LocalDateTime answeredAt; // 답변 시각

    @Column(nullable = false)
    @Builder.Default
    private boolean secret = false; // 비밀글 여부

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;  // soft delete
}
