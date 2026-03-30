package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.QnaRequest;
import com.bookstore.shop.readme.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    // UC-M-020: 1:1 문의 등록
    @PostMapping("/write")
    public ResponseEntity<String> writeQuestion(@RequestBody QnaRequest request) {
        // 인증 로직 연동 전 임시 memberId 사용
        Long memberId = 1L;

        qnaService.writeQuestion(
                memberId,
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getIsSecret()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body("문의가 등록되었습니다.");
    }

    // 설계서 1-2: 문의 상세 조회 (비밀글 권한 체크는 Service에서 수행)
    @GetMapping("/{id}")
    public ResponseEntity<?> getQnaDetail(@PathVariable Long id) {
        Long memberId = 1L;
        return ResponseEntity.ok(qnaService.getQnaDetail(id, memberId));
    }

    // 설계서 1-2: 문의 삭제 (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        Long memberId = 1L;
        qnaService.deleteQuestion(id, memberId);
        return ResponseEntity.ok("문의가 삭제되었습니다.");
    }
}