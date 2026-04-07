<!-- ─── src/views/member/MyOrderDetailView.vue ─────────────────────────────── -->
<template>
  <div>
    <h2>주문 상세</h2>

    <div v-if="order">
      <p>주문번호: {{ order.orderNumber }}</p>
      <p>주문 상태: {{ order.orderStatus }}</p>
      <p>최종 금액: {{ order.finalPrice.toLocaleString() }}원</p>

      <!-- 주문 상품 목록 -->
      <div v-for="item in order.orderItems" :key="item.orderItemId">
        <p>{{ item.productName }} × {{ item.quantity }} — {{ item.price.toLocaleString() }}원</p>
      </div>

      <!-- 배송 정보 -->
      <p>배송지: {{ order.deliveryAddress }} {{ order.deliveryAddressDetail }}</p>
      <p>배송 메모: {{ order.deliveryMemo || '없음' }}</p>

      <!-- 결제 정보 -->
      <p>결제 수단: {{ order.paymentProvider }}</p>
      <p>결제 상태: {{ order.paymentStatus }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/axios'

const route = useRoute()
const order = ref(null) // 주문 상세 데이터

// ── 마운트 시 주문 상세 조회 ──────────────────────────────────────────────────
onMounted(async () => {
  try {
    // URL 파라미터에서 orderId 추출
    const orderId = route.params.orderId

    // GET /api/order/{orderId} → 주문 상세 (OrderDetailResponse)
    const { data } = await api.get(`/api/order/${orderId}`)
    order.value = data
  } catch (e) {
    console.error('주문 상세 조회 실패', e)
  }
})
</script>