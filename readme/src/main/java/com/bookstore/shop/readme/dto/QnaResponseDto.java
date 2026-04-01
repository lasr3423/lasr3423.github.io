package com.bookstore.shop.readme.dto;

import com.bookstore.shop.readme.domain.Qna;
import com.bookstore.shop.readme.domain.QnaStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QnaResponseDto {

    private Long id;
    private Long parentId;
    private int depth;
    private Long memberId;
    private String memberName;
    private String category;
    private String title;
    private String content;
    private int viewCount;
    private QnaStatus qnaStatus;
    private boolean isSecret;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
    private List<QnaResponseDto> children;

    public static QnaResponseDto from(Qna qna) {
        return QnaResponseDto.builder()
                .id(qna.getId())
                .parentId(qna.getParent() != null ? qna.getParent().getId() : null)
                .depth(qna.getDepth())
                .memberId(qna.getMember().getId())
                .memberName(qna.getMember().getName())
                .category(qna.getCategory())
                .title(qna.getTitle())
                .content(qna.getContent())
                .viewCount(qna.getViewCount())
                .qnaStatus(qna.getQnaStatus())
                .isSecret(qna.isSecret())
                .answeredAt(qna.getAnsweredAt())
                .createdAt(qna.getCreatedAt())
                .build();
    }

    public static QnaResponseDto withChildren(Qna qna, List<QnaResponseDto> children) {
        return QnaResponseDto.builder()
                .id(qna.getId())
                .parentId(qna.getParent() != null ? qna.getParent().getId() : null)
                .depth(qna.getDepth())
                .memberId(qna.getMember().getId())
                .memberName(qna.getMember().getName())
                .category(qna.getCategory())
                .title(qna.getTitle())
                .content(qna.getContent())
                .viewCount(qna.getViewCount())
                .qnaStatus(qna.getQnaStatus())
                .isSecret(qna.isSecret())
                .answeredAt(qna.getAnsweredAt())
                .createdAt(qna.getCreatedAt())
                .children(children)
                .build();
    }
}