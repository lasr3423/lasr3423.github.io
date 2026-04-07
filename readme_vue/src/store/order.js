// ─── src/store/order.js ─────────────────────────────────────────────────────
import { defineStore } from 'pinia'
import { ref } from 'vue'

// useOrderStore : 주문 정보를 결제 화면으로 전달하는 전역 상태
// 사용 흐름: OrderView(주문 생성) → setOrder() → PaymentView(결제 진행)
export const useOrderStore = defineStore('order', () => {

  // 현재 결제할 주문 ID — 백엔드 Order 테이블 PK
  const orderId = ref(null)

  // 결제 금액 — 백엔드 order.finalPrice 와 반드시 일치해야 함 (탬퍼링 방지)
  const finalPrice = ref(0)

  // 결제창에 표시할 상품명 (예: "스프링 부트 입문 외 2건")
  const itemName = ref('')

  // 주문 정보 세팅 — OrderView에서 POST /api/order 성공 후 호출
  function setOrder(id, price, name) {
    orderId.value = id       // 백엔드에서 받은 orderId
    finalPrice.value = price // 백엔드에서 받은 finalPrice
    itemName.value = name    // 대표 상품명
  }

  // 결제 완료 또는 취소 후 초기화
  function clearOrder() {
    orderId.value = null
    finalPrice.value = 0
    itemName.value = ''
  }

  // 외부에서 사용할 값과 함수 노출
  return { 
    orderId, 
    finalPrice, 
    itemName, 
    setOrder, 
    clearOrder 
}
})