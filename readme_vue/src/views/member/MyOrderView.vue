<template>
  <div>
    <h2>주문 내역</h2>

    <!-- 주문이 없을 때 -->
    <p v-if="orders.length === 0">주문 내역이 없습니다.</p>

    <!-- 주문 목록 -->
    <div v-for="order in orders" :key="order.orderId">
      <p>주문번호: {{ order.orderNumber }}</p>
      <p>결제 금액: {{ order.finalPrice.toLocaleString() }}원</p>
      <p>주문 상태: {{ order.orderStatus }}</p>
      <p>주문일: {{ order.createdAt }}</p>

      <!-- 주문 상세 보기 -->
      <button @click="goDetail(order.orderId)">상세 보기</button>

      <!-- 주문 취소 (PAYED 상태에서만 가능) -->
      <button
        v-if="order.orderStatus === 'PAYED'"
        @click="cancelOrder(order.orderId)">
        주문 취소
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()
const orders = ref([]) // 주문 목록

// ── 마운트 시 주문 목록 조회 ──────────────────────────────────────────────────
onMounted(async () => {
  try {
    // GET /api/order → 내 주문 목록 (최신순)
    const { data } = await api.get('/api/order')
    orders.value = data.content // Page<OrderSummaryResponse>.content
  } catch (e) {
    console.error('주문 목록 조회 실패', e)
  }
})

// ── 주문 상세 이동 ────────────────────────────────────────────────────────────
function goDetail(orderId) {
  router.push(`/mypage/order/${orderId}`)
}

// ── 주문 취소 ─────────────────────────────────────────────────────────────────
async function cancelOrder(orderId) {
  if (!confirm('주문을 취소하시겠습니까?')) return

  try {
    // DELETE /api/order/{orderId} → 주문 취소 + PG사 환불 처리
    await api.delete(`/api/order/${orderId}`, {
      data: { cancelReason: '고객 요청' } // request body
    })
    alert('주문이 취소되었습니다.')

    // 목록 새로고침
    const { data } = await api.get('/api/order')
    orders.value = data.content
  } catch (e) {
    alert('주문 취소에 실패했습니다.')
    console.error(e)
  }
}
</script>