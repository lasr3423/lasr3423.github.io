<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  // 토스가 failUrl에 code, message를 쿼리파라미터로 전달
  const { orderId, code, message } = route.query

  // 서버에 실패 사실 알림 → Payment.failureReason 저장
  await axios.post('/api/order/payment/fail', {
    orderId: Number(orderId),
    code,     // 토스 에러 코드 (예: "USER_CANCEL")
    message,  // 에러 메시지 (DB failure_reason 컬럼에 저장)
  })

  alert(`결제 실패: ${message}`)
  router.push('/cart')  // 장바구니로 돌아가기
})
</script>