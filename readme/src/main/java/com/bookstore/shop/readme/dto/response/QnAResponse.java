package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.QnA;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QnAResponse {

    private final Long id;
    private final Long productId;
    private final String productTitle;
    private final String title;
    private final String content;   // 비밀글이면 작성자/관리자만 볼 수 있음
    private final boolean secret;
    private final String answer;    // null = 미답변
    private final String answeredByName;
    private final LocalDateTime answeredAt;
    private final String memberName;
    private final LocalDateTime createdAt;

    public QnAResponse(QnA qna) {
        this.id             = qna.getId();
        this.productId      = qna.getProduct() != null ? qna.getProduct().getId() : null;
        this.productTitle   = qna.getProduct() != null ? qna.getProduct().getTitle() : null;
        this.title          = qna.getTitle();
        this.content        = qna.getContent();
        this.secret         = qna.isSecret();
        this.answer         = qna.getAnswer();
        this.answeredByName = qna.getAnsweredBy() != null ? qna.getAnsweredBy().getName() : null;
        this.answeredAt     = qna.getAnsweredAt();
        this.memberName     = qna.getMember().getName();
        this.createdAt      = qna.getCreatedAt();
    }
}
