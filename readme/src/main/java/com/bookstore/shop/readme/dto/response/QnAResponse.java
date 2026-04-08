package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.QnA;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class QnAResponse {

    private final Long id;
    private final Long qnaId;
    private final Long parentId;
    private final int depth;
    private final String category;
    private final String title;
    private final String content;
    private final String qnaStatus;
    private final boolean isSecret;
    private final boolean secret;
    private final int viewCount;
    private final Long memberId;
    private final String memberName;
    private final String answer;
    private final LocalDateTime answeredAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<QnAResponse> children;

    public QnAResponse(QnA qna) {
        this.id = qna.getId();
        this.qnaId = qna.getId();
        this.parentId = qna.getParent() != null ? qna.getParent().getId() : null;
        this.depth = qna.getDepth();
        this.category = qna.getCategory();
        this.title = qna.getTitle();
        this.content = qna.getContent();
        this.qnaStatus = qna.getQnaStatus() != null ? qna.getQnaStatus().name() : null;
        this.isSecret = qna.isSecret();
        this.secret = qna.isSecret();
        this.viewCount = qna.getViewCount();
        this.memberId = qna.getMember().getId();
        this.memberName = qna.getMember().getName();
        this.answer = qna.getChildren().stream()
                .filter(child -> child.getDeletedAt() == null)
                .filter(child -> child.getDepth() == qna.getDepth() + 1)
                .map(QnA::getContent)
                .findFirst()
                .orElse(null);
        this.answeredAt = qna.getAnsweredAt();
        this.createdAt = qna.getCreatedAt();
        this.updatedAt = qna.getUpdatedAt();
        this.children = qna.getChildren().stream()
                .filter(child -> child.getDeletedAt() == null)
                .map(QnAResponse::new)
                .toList();
    }
}
