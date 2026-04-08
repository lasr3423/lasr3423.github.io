package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * QnA (1:1 문의) 엔티티 — DB 설계서 v1.3 기준
 *
 * 계층형 구조 (parent_id / depth):
 *   depth=0 : 사용자 최초 질문
 *   depth=1 : 관리자 답변 (parent = depth-0 질문)
 *   depth=2 : 사용자 재문의 (parent = depth-1 답변)
 *   depth=3 : 관리자 재답변
 *   depth=4 : 최대 깊이
 *
 * ※ 기존 임시 구현(answer, answeredBy 컬럼)을 DB 설계서 기준으로 제거.
 *    답변은 별도 QnA 레코드로 저장.
 */
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "qna")
public class QnA extends BaseEntity {

    // ── 계층 구조 ─────────────────────────────────────────────────────────

    /** 부모 QnA (null 이면 최상위 질문) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QnA parent;

    /** 자식 QnA 목록 (답변, 재문의 등) */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnA> children = new ArrayList<>();

    /** 계층 깊이: 0=질문, 1=답변, 2=재문의, 3=재답변, 4=최대 */
    @Column(nullable = false)
    @Builder.Default
    private int depth = 0;

    // ── 작성자 / 연관 상품 ────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // ── 문의 내용 ─────────────────────────────────────────────────────────

    /** 문의 유형 (배송, 환불, 상품 문의 등) — depth=0 질문에만 유효 */
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String category = "기타";

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // ── 상태 / 통계 ───────────────────────────────────────────────────────

    /** QnA 답변 상태 — depth=0 질문에만 유효 */
    @Enumerated(EnumType.STRING)
    @Column(name = "qna_status", nullable = false)
    @Builder.Default
    private QnaStatus qnaStatus = QnaStatus.WAITING;

    @Column(name = "is_secret", nullable = false)
    @Builder.Default
    private boolean isSecret = false;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    /** 답변 완료 일시 (depth=0 질문에 COMPLETE 처리될 때 기록) */
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // ── Soft Delete ───────────────────────────────────────────────────────

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
