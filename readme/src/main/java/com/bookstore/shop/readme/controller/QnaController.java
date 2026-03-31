package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Qna;
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

    /**
     * [설계서 13번 테이블 정의서 준수]
     * UC-M-020: 1:1 문의 등록
     * 반영: category, title, content, is_secret 필드 매핑
     */
    @PostMapping("/write")
    public ResponseEntity<String> writeQuestion(@RequestBody QnaRequest request) {
        // 인증 로직 연동 전 임시 memberId 사용 (설계서 member_id 대응)
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

    /**
     * [설계서 v1.3 제약조건 준수]
     * 반영 1: 서비스 레이어의 Soft Delete(deleted_at) 검증 결과 반환
     * 반영 2: 상세 조회 시 view_count 증가 로직 연동
     */
    @GetMapping("/{id}")
    public ResponseEntity<Qna> getQnaDetail(@PathVariable Long id) {
        // 실제 운영 시 SecurityContext에서 memberId 추출 필요
        Long memberId = 1L;

        // Service에서 비밀글 권한 및 view_count 증가 처리가 수행됨
        return ResponseEntity.ok(qnaService.getQnaDetail(id, memberId));
    }

    /**
     * [설계서 Soft Delete 준수]
     * 반영: DB 실제 삭제가 아닌 deleted_at 업데이트 요청
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        Long memberId = 1L;
        qnaService.deleteQuestion(id, memberId);
        return ResponseEntity.ok("문의가 삭제되었습니다.");
    }
}