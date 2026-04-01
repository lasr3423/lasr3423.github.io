package com.bookstore.shop.readme.dto;

import com.bookstore.shop.readme.domain.QnaStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class QnaRequestDto {

    // FQ-002: 질문 등록
    @Getter
    @NoArgsConstructor
    public static class Write {
        private String category;
        private String title;
        private String content;
        private boolean isSecret;
    }

    // FQ-004: n차 재문의
    @Getter
    @NoArgsConstructor
    public static class Reply {
        private String title;
        private String content;
    }

    // FA-035: 관리자 답변 등록
    @Getter
    @NoArgsConstructor
    public static class Answer {
        private String content;
    }

    // FM-023 / FA-036: 수정
    @Getter
    @NoArgsConstructor
    public static class Update {
        private String content;
    }

    // FA-034: 상태 변경
    @Getter
    @NoArgsConstructor
    public static class StatusUpdate {
        private QnaStatus qnaStatus;
    }
}