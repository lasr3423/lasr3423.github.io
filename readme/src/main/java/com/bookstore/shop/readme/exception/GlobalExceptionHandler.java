package com.bookstore.shop.readme.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 유효성 검사 실패 → 첫 번째 필드 에러 메시지 반환
     * 예: 이메일 형식 오류, 비밀번호 길이 부족, 전화번호 패턴 불일치 등
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    /**
     * 비즈니스 로직 예외 (RuntimeException)
     * - 로그인 실패: "이메일 혹은 패스워드 오류입니다."
     * - 중복 이메일, 계정 제한, 토큰 만료 등 서비스 레이어 예외 포함
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    /**
     * 그 외 예상치 못한 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
