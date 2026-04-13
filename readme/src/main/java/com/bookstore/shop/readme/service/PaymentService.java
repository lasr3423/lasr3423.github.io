package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.*;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.gateway.*;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// @Service : 비즈니스 로직 계층 빈 등록
// @RequiredArgsConstructor : final 필드 생성자 주입 자동 생성
@Service
@RequiredArgsConstructor
public class PaymentService {

    // Repository: DB 접근 계층
    private final PaymentRepository  paymentRepository;
    private final OrderRepository    orderRepository;
    private final CartRepository     cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    // Gateway: 각 PG사 API 호출 계층 (전략 패턴 구현체)
    private final TossPaymentGateway tossPaymentGateway;
    private final KakaoPaymentGateway kakaoPaymentGateway;
    private final NaverPaymentGateway naverPaymentGateway;

    // ─── 카카오/네이버 결제 준비 ───────────────────────────────────────────

    // @Transactional : 이 메서드 실행 중 예외 발생 시 DB 변경사항 자동 롤백
    @Transactional
    public PaymentReadyResponse requestReady(PaymentReadyRequest request, Long memberId) {

        // 1. 주문 소유권 확인 — memberId 검증으로 타인의 주문에 결제 시도 방지
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. provider 문자열로 해당 Gateway 구현체 선택 (전략 패턴 실행부)
        request.setMemberId(memberId);
        PaymentGateway gateway = resolveGateway(request.getProvider());

        // 3. PG사에 결제 준비 API 호출 → tid 또는 paymentId + 결제창 URL 발급
        PaymentReadyResponse response = gateway.ready(request);

        // 4. READY 상태의 Payment 레코드 생성 및 저장
        //    — confirm/approve 시 이 레코드를 찾아서 상태 업데이트
        Payment payment = Payment.builder()
                .order(order)
                .paymentProvider(PaymentProvider.valueOf(request.getProvider())) // "KAKAO" → KAKAO ENUM
                .paymentStatus(PaymentStatus.READY)                       // 초기 상태
                .amount(order.getFinalPrice())                             // ⚠️ 서버에서 금액 직접 세팅
                // 카카오: tid 저장 / 네이버: paymentId 저장 (둘 중 하나만 값 있음)
                .pgTid(response.getTid() != null ? response.getTid() : response.getPaymentId())
                .build();
        paymentRepository.save(payment);

        // 5. 프론트에 결제창 URL 응답 (프론트는 이 URL로 리다이렉트)
        return response;
    }

    @Transactional
    public void completeBankTransfer(BankTransferRequest request, Long memberId) {
        if (request == null || request.orderId() == null) {
            throw new RuntimeException("주문 정보가 올바르지 않습니다.");
        }

        Order order = orderRepository.findByIdAndMemberId(request.orderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        Payment payment = paymentRepository.findByOrderId(request.orderId())
                .orElse(Payment.builder()
                        .order(order)
                        .paymentProvider(PaymentProvider.BANK_TRANSFER)
                        .amount(order.getFinalPrice())
                        .build());

        payment.setPaymentProvider(PaymentProvider.BANK_TRANSFER);
        payment.setMethod("계좌이체");
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        clearOrderedCartItems(order.getId(), memberId);
    }

    // ─── 토스 결제 최종 승인 (/confirm) ──────────────────────────────────

    @Transactional
    public void confirmToss(PaymentConfirmRequest request, Long memberId) {

        // 1. 주문 소유권 확인
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. ⚠️ 금액 검증 필수 — 탬퍼링 방지
        //    프론트에서 amount를 임의로 수정해서 보낼 수 있음
        //    서버에서 order.getFinalPrice()와 비교해야 함
        validateAmount(request.getOrderId(), request.getAmount());

        // 3. 토스 confirm API 호출 → 실제 결제 확정
        PaymentConfirmResponse response = tossPaymentGateway.confirm(request);

        // 4. 결제 레코드 조회 또는 신규 생성 (ready() 없이 바로 confirm 오는 경우 대비)
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElse(Payment.builder()
                        .order(order)
                        .paymentProvider(PaymentProvider.TOSS)
                        .amount(request.getAmount())
                        .build());

        // 5. 결제 완료 상태로 업데이트
        payment.setPaymentKey(request.getPaymentKey()); // 취소 시 필요
        payment.setMethod(resolveTossMethod(request.getMethod()));
        payment.setPaymentStatus(PaymentStatus.PAID);   // READY → PAID
        payment.setPaidAt(LocalDateTime.now());          // 결제 완료 시각 기록
        paymentRepository.save(payment);

        // 6. 주문 상태를 PAYMENT_PENDING → PENDING 으로 전환 (결제 확인 완료)
        order.setOrderStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // 7. 결제 성공 확정 후 장바구니 아이템 삭제
        //    주문에 포함된 상품들만 골라서 삭제 (다른 세션 장바구니에 영향 없음)
        clearOrderedCartItems(order.getId(), memberId);
    }

    // ─── 카카오/네이버 결제 최종 승인 (/approve) ─────────────────────────

    @Transactional
    public void approvePayment(PaymentApproveRequest request, Long memberId) {

        // 1. 주문 소유권 확인
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. ready() 시 저장한 Payment 레코드 조회 (pgTid, amount 가져오기 위해)
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 3. DB에 저장된 amount로 검증 (프론트에서 amount 안 보냄 → DB값 사용)
        validateAmount(request.getOrderId(), payment.getAmount());

        String provider = request.getProvider();
        String resolvedTid = hasText(request.getTid()) ? request.getTid() : payment.getPgTid();
        String resolvedPaymentId = hasText(request.getPaymentId()) ? request.getPaymentId() : payment.getPgTid();

        // 4. approve API에 전달할 파라미터 빌드
        //    (카카오: tid + pgToken 필요 / 네이버: paymentId 필요)
        PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                .orderId(request.getOrderId())
                .memberId(memberId)
                .tid(resolvedTid)           // 카카오 tid
                .pgToken(request.getPgToken())   // 카카오 pg_token
                .paymentId(resolvedPaymentId) // 네이버 paymentId
                .build();

        // 5. provider로 Gateway 구현체 선택 후 confirm() 호출
        PaymentGateway gateway = resolveGateway(provider);
        gateway.confirm(confirmRequest);

        // 6. 결제 완료 상태로 업데이트
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 7. 주문 상태를 PAYMENT_PENDING → PENDING 으로 전환 (결제 확인 완료)
        order.setOrderStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        clearOrderedCartItems(order.getId(), memberId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // ─── 결제 실패 처리 (토스 failUrl) ───────────────────────────────────

    @Transactional
    public void failPayment(PaymentFailRequest request) {
        // Payment 레코드가 있으면 실패 상태로 업데이트
        // ready() 없이 실패하는 경우엔 레코드가 없을 수 있으므로 ifPresent 사용
        paymentRepository.findByOrderId(request.getOrderId()).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(request.getMessage()); // 토스 에러 메시지 저장
            paymentRepository.save(payment);
        });
    }

    // ─── 결제 취소 / 환불 ─────────────────────────────────────────────────

    @Transactional
    public void cancelPayment(Long orderId, String cancelReason, Integer cancelAmount) {

        // 1. Payment 레코드 조회 — provider, paymentKey/pgTid 가져오기
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        if (payment.getPaymentStatus() == PaymentStatus.CANCELLED) {
            return;
        }

        if (payment.getPaymentStatus() == PaymentStatus.READY) {
            payment.setPaymentStatus(PaymentStatus.CANCELLED);
            payment.setCancelReason(cancelReason);
            payment.setRefundedAmount(0);
            payment.setReturnFee(0);
            payment.setCancelledAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return;
        }

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("취소 가능한 결제 상태가 아닙니다.");
        }

        int requestedCancelAmount = cancelAmount != null ? cancelAmount : payment.getAmount();
        if (requestedCancelAmount < 0 || requestedCancelAmount > payment.getAmount()) {
            throw new RuntimeException("취소 금액이 올바르지 않습니다.");
        }
        int returnFee = Math.max(payment.getAmount() - requestedCancelAmount, 0);

        if (payment.getPaymentProvider() == PaymentProvider.BANK_TRANSFER) {
            payment.setPaymentStatus(PaymentStatus.CANCELLED);
            payment.setCancelReason(cancelReason);
            payment.setRefundedAmount(requestedCancelAmount);
            payment.setReturnFee(returnFee);
            payment.setCancelledAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return;
        }

        // 2. PG사별 취소 파라미터 빌드 (모든 PG사 정보를 하나의 DTO에 담음)
        PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                .paymentKey(payment.getPaymentKey()) // 토스 취소 API URL path variable
                .tid(payment.getPgTid())             // 카카오 취소 body "tid"
                .paymentId(payment.getPgTid())       // 네이버 취소 body "paymentId"
                .cancelAmount(requestedCancelAmount) // 부분 취소 포함
                .cancelReason(cancelReason)          // 사용자 입력 취소 사유
                .provider(payment.getPaymentProvider().name()) // "TOSS" / "KAKAO" / "NAVER"
                .build();

        // 3. provider로 Gateway 구현체 선택 후 cancel() 호출
        PaymentGateway gateway = resolveGateway(payment.getPaymentProvider().name());
        gateway.cancel(cancelRequest);

        // 4. 취소 완료 상태로 업데이트
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setCancelReason(cancelReason);
        payment.setRefundedAmount(requestedCancelAmount);
        payment.setReturnFee(returnFee);
        payment.setCancelledAt(LocalDateTime.now()); // 취소 완료 시각 기록
        paymentRepository.save(payment);
    }

    // ─── 마이페이지 내 결제 내역 조회 ────────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PaymentStatusResponse>> getMyPayments(Long memberId, Pageable pageable) {
        return ResponseEntity.ok(
                paymentRepository.findByOrder_MemberId(memberId, pageable)
                        .map(PaymentStatusResponse::new));
    }

    // ─── 관리자 전체 결제 내역 조회 (상태 필터 선택) ───────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PaymentStatusResponse>> getAllPayments(String status, Pageable pageable) {
        Page<PaymentStatusResponse> result;
        if (status != null && !status.isBlank()) {
            result = paymentRepository
                    .findAllByPaymentStatus(PaymentStatus.valueOf(status), pageable)
                    .map(PaymentStatusResponse::new);
        } else {
            result = paymentRepository.findAll(pageable).map(PaymentStatusResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<PaymentStatusResponse> getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
        return ResponseEntity.ok(new PaymentStatusResponse(payment));
    }

    // ─── 내부 유틸 메서드 ─────────────────────────────────────────────────

    // 금액 검증 — 탬퍼링 방지 핵심 메서드
    // 클라이언트에서 보낸 amount를 절대 신뢰하지 말 것
    // DB의 order.getFinalPrice()와 반드시 비교
    private void validateAmount(Long orderId, Integer amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        if (!order.getFinalPrice().equals(amount)) {
            // 금액 불일치 → 결제 탬퍼링 시도로 판단, 강제 예외 발생
            throw new RuntimeException("결제 금액이 일치하지 않습니다.");
        }
    }

    private String resolveTossMethod(String method) {
        if ("TRANSFER".equalsIgnoreCase(method)) {
            return "계좌이체";
        }
        if ("TOSSPAY".equalsIgnoreCase(method)) {
            return "토스페이";
        }
        return "카드";
    }

    // provider 문자열로 Gateway 구현체 선택
    // Java 14+ switch expression 사용
    private PaymentGateway resolveGateway(String provider) {
        return switch (provider.toUpperCase()) {
            case "TOSS"  -> tossPaymentGateway;   // 토스 구현체 반환
            case "KAKAO" -> kakaoPaymentGateway;  // 카카오 구현체 반환
            case "NAVER" -> naverPaymentGateway;  // 네이버 구현체 반환
            case "BANK_TRANSFER" -> throw new RuntimeException("계좌이체는 별도 PG 연동 대상이 아닙니다.");
            default -> throw new RuntimeException("지원하지 않는 결제 수단입니다: " + provider);
        };
    }

    /**
     * 결제 완료된 주문의 상품에 해당하는 장바구니 아이템만 삭제
     *
     * - OrderItem에서 주문된 productId 목록을 추출
     * - 해당 회원의 장바구니에서 그 상품들만 찾아서 삭제
     * - 다른 회원의 장바구니나 주문에 포함되지 않은 아이템은 건드리지 않음
     */
    private void clearOrderedCartItems(Long orderId, Long memberId) {
        // 1. 이 주문에 포함된 상품 ID 목록 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        Set<Long> orderedProductIds = orderItems.stream()
                .map(oi -> oi.getProduct().getId())
                .collect(Collectors.toSet());

        // 2. 해당 회원의 장바구니 조회 (없으면 아무것도 안 함)
        cartRepository.findByMemberId(memberId).ifPresent(cart -> {
            // 3. 주문된 상품에 해당하는 아이템만 필터링해서 삭제
            List<CartItem> toRemove = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId())
                    .stream()
                    .filter(ci -> orderedProductIds.contains(ci.getProduct().getId()))
                    .collect(Collectors.toList());
            cartItemRepository.deleteAll(toRemove);
        });
    }
}
