package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.QnA;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QnA 응답 DTO — DB설계서 기준 트리 구조
 *
 * depth=0: 사용자 최초 질문 (category, qnaStatus, isSecret 유효)
 * depth=1: 관리자 답변
 * depth=2: 사용자 재문의
 * depth=3: 관리자 재답변
 * depth=4: 최대 깊이
 *
 * children: 이 QnA의 직접 자식 목록 (답변, 재문의 등)
 * isSecret=true + 비작성자 조회 시 content는 Service 레이어에서 마스킹 처리
 */
@Getter
public class QnAResponse {

    private final Long   id;
    private final Long   parentId;         // null이면 최상위 질문
    private final int    depth;            // 0=질문, 1=답변, 2=재문의, ...
    private final String category;         // 문의 유형 (depth=0에만 유효)
    private final String title;
    private final String content;
    private final String qnaStatus;        // WAITING / PROCESSING / COMPLETE
    private final boolean isSecret;
    private final int    viewCount;
    private final Long   memberId;
    private final String memberName;
    private final LocalDateTime answeredAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<QnAResponse> children; // 자식 QnA 목록 (재귀 구조)

    public QnAResponse(QnA qna) {
        this.id         = qna.getId();
        this.parentId   = qna.getParent() != null ? qna.getParent().getId() : null;
        this.depth      = qna.getDepth();
        this.category   = qna.getCategory();
        this.title      = qna.getTitle();
        this.content    = qna.getContent();
        this.qnaStatus  = qna.getQnaStatus() != null ? qna.getQnaStatus().name() : null;
        this.isSecret   = qna.isSecret();
        this.viewCount  = qna.getViewCount();
        this.memberId   = qna.getMember().getId();
        this.memberName = qna.getMember().getName();
        this.answeredAt = qna.getAnsweredAt();
        this.createdAt  = qna.getCreatedAt();
        this.updatedAt  = qna.getUpdatedAt();
        // 삭제되지 않은 자식만 포함
        this.children   = qna.getChildren().stream()
                .filter(child -> child.getDeletedAt() == null)
                .map(QnAResponse::new)
                .toList();
    }
}
