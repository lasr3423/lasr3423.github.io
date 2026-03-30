package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.NoticeRequest;
import com.bookstore.shop.readme.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    /**
     * [설계서 1-2] 공지사항 등록
     */
    @PostMapping
    public ResponseEntity<String> createNotice(@RequestBody NoticeRequest request) {
        // 인증 모듈 연동 전 임시 관리자 ID 사용 (설계서 member_id 대응)
        Long adminId = 1L;
        noticeService.createNotice(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("공지사항이 등록되었습니다.");
    }

    /**
     * [설계서 1-2] 공지사항 수정
     */
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequest request) {
        noticeService.updateNotice(id, request);
        return ResponseEntity.ok("공지사항이 수정되었습니다.");
    }

    /**
     * [설계서 1-2] 공지사항 삭제 (Soft Delete)
     * 제약조건: DB에서 삭제하지 않고 deletedAt 업데이트
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok("공지사항이 삭제되었습니다.");
    }
}