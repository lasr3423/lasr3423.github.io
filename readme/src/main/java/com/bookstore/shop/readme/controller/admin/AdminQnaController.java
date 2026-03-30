package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.QnaAnswerRequest;
import com.bookstore.shop.readme.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/qna")
@RequiredArgsConstructor
public class AdminQnaController {

    private final QnaService qnaService;

    // UC-A-010: 관리자 답변 등록
    @PostMapping("/{id}/answer")
    public ResponseEntity<String> answerQuestion(
            @PathVariable("id") Long parentId,
            @RequestBody QnaAnswerRequest request) {

        // 인증 로직 연동 전 임시 관리자 ID 사용
        Long adminId = 999L;

        qnaService.answerQuestion(parentId, adminId, request.getContent());

        return ResponseEntity.status(HttpStatus.CREATED).body("답변이 등록되었습니다.");
    }
}