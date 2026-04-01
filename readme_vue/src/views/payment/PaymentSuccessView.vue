<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()  // 현재 URL의 쿼리파라미터 접근
const router = useRouter() // 프로그래밍 방식 페이지 이동

onMounted(async () => {
  // 토스가 successUrl에 쿼리파라미터로 paymentKey, orderId, amount를 전달
  const { paymentKey, orderId, amount } = route.query

  try {
    // 서버의 /confirm 엔드포인트에 POST 요청 → 백엔드에서 토스 API 최종 승인 처리
    await axios.post('/api/order/payment/confirm', {
      paymentKey,
      orderId: Number(orderId),  // 쿼리파라미터는 String → Number 변환 필요
      amount: Number(amount),    // 쿼리파라미터는 String → Number 변환 필요
    })
    router.push('/mypage/order')  // 결제 완료 → 주문 내역 페이지 이동
  } catch (e) {
    alert('결제 승인 중 오류가 발생했습니다.')
    router.push('/cart')  // 오류 → 장바구니로 돌아가기
  }
})
</script>