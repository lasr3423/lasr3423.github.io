<template>
  <div>
    <button @click="payWithToss">토스페이먼츠로 결제</button>
  </div>
</template>

<script setup>
import { useOrderStore } from '@/store/order'

const orderStore = useOrderStore()  // Pinia store에서 주문 정보 가져오기

const payWithToss = () => {
  // 1. clientKey로 TossPayments 인스턴스 생성 (공개 키 — 노출 가능)
  const clientKey = 'test_ck_AQ92ymxN34vmXlbmObdPrajRKXvd'
  const tossPayments = window.TossPayments(clientKey)

  // 2. 결제 요청 — 토스 결제창 오픈 (서버 API 호출 없음)
  tossPayments.requestPayment('카드', {
    amount: orderStore.finalPrice,            // 결제 금액 — 서버의 order.finalPrice와 일치해야 함
    orderId: String(orderStore.orderId),      // ⚠️ 토스 orderId는 String 타입 (DB ID와 다른 개념)
    orderName: '교보문고 도서 주문',            // 결제창에 표시될 주문명
    customerName: '구매자명',                  // 구매자 이름
    successUrl: `${window.location.origin}/payment/success`,  // 결제 성공 시 이동할 프론트 URL
    failUrl: `${window.location.origin}/payment/fail`,        // 결제 실패 시 이동할 프론트 URL
  })
}
</script>