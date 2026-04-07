<template>
  <div>
    <h2>주문서</h2>

    <!-- 주문 상품 목록 표시 -->
    <div v-for="item in orderItems" :key="item.cartItemId">
      <p>{{ item.productName }} × {{ item.quantity }} — {{ item.price.toLocaleString() }}원</p>
    </div>

    <!-- 최종 결제 금액 -->
    <p>최종 결제 금액: {{ finalPrice.toLocaleString() }}원</p>

    <!-- 배송지 입력 -->
    <input v-model="deliveryAddress"  placeholder="배송지 주소" />
    <input v-model="deliveryAddressDetail" placeholder="상세 주소" />
    <input v-model="deliveryMemo"     placeholder="배송 메모 (선택)" />

    <!-- 결제 수단 선택 -->
    <select v-model="selectedProvider">
      <option value="TOSS">토스페이먼츠 (카드)</option>
      <option value="KAKAO">카카오페이</option>
      <option value="NAVER">네이버페이</option>
    </select>

    <button @click="submitOrder">결제하기</button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'           // axios 인스턴스
import { useOrderStore } from '@/store/order' // 주문 정보 전역 상태

const router     = useRouter()
const orderStore = useOrderStore()

// ── 배송지 / 결제수단 입력값 ─────────────────────────────────────────────────
const deliveryAddress       = ref('')
const deliveryAddressDetail = ref('')
const deliveryMemo          = ref('')
const selectedProvider      = ref('TOSS') // 기본값: 토스

// ── 장바구니에서 넘어온 상품 목록 ────────────────────────────────────────────
// TODO: CartStore 또는 router state 로 전달받기
// 임시: 하드코딩 (Cart 구현 후 교체)
const orderItems = ref([
  // { cartItemId: 1, productName: '스프링 부트 입문', quantity: 2, price: 30000 }
])

// 최종 결제 금액 — 서버에서 계산한 값을 받아서 표시 (프론트 계산 신뢰 X)
const finalPrice = ref(0)

// ── 주문 생성 + 결제 화면 이동 ────────────────────────────────────────────────
async function submitOrder() {
  try {
    // 1. 백엔드에 주문 생성 요청
    // → POST /api/order
    // → 응답: { orderId, finalPrice, itemName }
    const { data } = await api.post('/api/order', {
      cartItemIds:          orderItems.value.map(i => i.cartItemId), // 체크된 cartItem ID 목록
      deliveryAddress:      deliveryAddress.value,
      deliveryAddressDetail: deliveryAddressDetail.value,
      deliveryMemo:         deliveryMemo.value,
      provider:             selectedProvider.value, // 결제수단 (서버에서 필요한 경우)
    })

    // 2. 응답받은 주문 정보를 Pinia store에 저장
    // → PaymentView.vue 에서 이 값을 읽어 결제 진행
    orderStore.setOrder(data.orderId, data.finalPrice, data.itemName)

    // 3. 결제 화면으로 이동
    router.push('/payment')

  } catch (e) {
    alert('주문 생성에 실패했습니다.')
    console.error(e)
  }
}
</script>