package com.bookstore.shop.readme.dto.request;

/**
 * QnA 등록 요청 DTO — DB설계서 기준
 *
 * parentId: null → depth=0 최초 질문 / 값 있음 → 해당 QnA의 자식(답변 or 재문의)
 * category: depth=0 질문에만 유효 (배송, 환불, 상품문의, 기타 등)
 * isSecret: 비밀글 여부
 */
public record QnACreateRequest(
        Long parentId,      // null이면 최초 질문, 값 있으면 자식 레코드 (답변/재문의)
        String category,    // 문의 유형 — depth=0 질문에만 유효, 기본값 "기타"
        String title,
        String content,
        boolean isSecret    // DB설계서 qna.is_secret
) {}
