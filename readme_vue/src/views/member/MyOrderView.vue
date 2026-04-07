<template>
  <div class="min-h-screen bg-slate-50 py-8">
    <div class="mx-auto max-w-3xl px-4">

      <h1 class="mb-6 text-2xl font-bold text-brand-800">주문 내역</h1>

      <!-- 로딩 -->
      <div v-if="loading" class="flex justify-center py-16">
        <span class="text-slate-400">주문 내역을 불러오는 중...</span>
      </div>

      <!-- 주문 없음 -->
      <div
        v-else-if="orders.length === 0"
        class="rounded-2xl border border-slate-200 bg-white py-16 text-center text-slate-400"
      >
        주문 내역이 없습니다.
      </div>

      <!-- 주문 목록 -->
      <div v-else class="space-y-4">
        <div
          v-for="order in orders"
          :key="order.orderId"
          class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <p class="text-xs text-slate-400">{{ formatDate(order.orderAt) }}</p>
              <p class="mt-1 text-sm font-medium text-slate-500">주문번호: <span class="font-mono text-slate-700">{{ order.number }}</span></p>
              <p class="mt-2 text-lg font-bold text-brand-800">{{ order.finalPrice?.toLocaleString() }}원</p>
            </div>
            <span
              class="shrink-0 rounded-full px-3 py-1 text-xs font-semibold"
              :class="statusClass(order.orderStatus)"
            >
              {{ statusLabel(order.orderStatus) }}
            </span>
          </div>

          <div class="mt-4 flex flex-wrap gap-2">
            <button
              class="rounded-xl border border-brand-800 px-4 py-2 text-sm font-semibold text-brand-800 transition hover:bg-brand-50"
              @click="goDetail(order.orderId)"
            >
              상세 보기
            </button>
            <button
              v-if="order.orderStatus === 'PENDING' || order.orderStatus === 'PAYED'"
              class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600"
              @click="cancelOrder(order.orderId)"
            >
              주문 취소
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { memberApi } from '@/api/member'
import { orderApi } from '@/api/order'

const router  = useRouter()
const orders  = ref([])
const loading = ref(false)

// ── 마운트 시 주문 목록 조회 ──────────────────────────────────────────────────
onMounted(async () => {
  loading.value = true
  try {
    // GET /api/member/me/orders → 내 주문 목록
    const { data } = await memberApi.getOrders({ page: 0, size: 20, sort: 'createdAt,desc' })
    orders.value = data.content ?? []
  } catch (e) {
    console.error('주문 목록 조회 실패', e)
  } finally {
    loading.value = false
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
    await orderApi.cancel(orderId, '고객 요청')
    alert('주문이 취소되었습니다.')
    const { data } = await memberApi.getOrders({ page: 0, size: 20, sort: 'createdAt,desc' })
    orders.value = data.content ?? []
  } catch (e) {
    alert('주문 취소에 실패했습니다.')
    console.error(e)
  }
}

// ── 날짜 포맷 ─────────────────────────────────────────────────────────────────
function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric', month: 'long', day: 'numeric',
  })
}

// ── 상태 텍스트 ───────────────────────────────────────────────────────────────
function statusLabel(status) {
  const map = {
    PENDING:    '결제 대기',
    PAYED:      '결제 완료',
    APPROVAL:   '배송 준비',
    DELIVERING: '배송 중',
    DELIVERED:  '배송 완료',
    CANCELED:   '취소됨',
  }
  return map[status] ?? status
}

// ── 상태별 색상 클래스 ─────────────────────────────────────────────────────────
function statusClass(status) {
  if (status === 'CANCELED')   return 'bg-rose-50 text-rose-600'
  if (status === 'PENDING')    return 'bg-amber-50 text-amber-600'
  if (status === 'PAYED')      return 'bg-blue-50 text-blue-600'
  if (status === 'APPROVAL')   return 'bg-violet-50 text-violet-600'
  if (status === 'DELIVERING') return 'bg-sky-50 text-sky-600'
  if (status === 'DELIVERED')  return 'bg-emerald-50 text-emerald-600'
  return 'bg-slate-100 text-slate-500'
}
</script>
