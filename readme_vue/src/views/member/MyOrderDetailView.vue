<template>
  <div class="min-h-screen bg-slate-50 py-8">
    <div class="mx-auto max-w-3xl px-4">

      <!-- 뒤로가기 -->
      <button
        class="mb-4 flex items-center gap-1 text-sm text-slate-500 transition hover:text-brand-800"
        @click="router.push('/mypage/order')"
      >
        ← 주문 목록으로
      </button>

      <h1 class="mb-6 text-2xl font-bold text-brand-800">주문 상세</h1>

      <!-- 로딩 -->
      <div v-if="loading" class="flex justify-center py-16">
        <span class="text-slate-400">불러오는 중...</span>
      </div>

      <!-- 에러 -->
      <div v-else-if="!order" class="rounded-2xl border border-slate-200 bg-white py-16 text-center text-slate-400">
        주문 정보를 찾을 수 없습니다.
      </div>

      <template v-else>

        <!-- 주문 요약 -->
        <section class="mb-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs text-slate-400">{{ formatDate(order.orderAt) }}</p>
              <p class="mt-1 text-sm text-slate-500">주문번호: <span class="font-mono font-medium text-slate-700">{{ order.number }}</span></p>
            </div>
            <span
              class="shrink-0 rounded-full px-3 py-1 text-xs font-semibold"
              :class="statusClass(order.orderStatus)"
            >
              {{ statusLabel(order.orderStatus) }}
            </span>
          </div>
          <div class="mt-4 grid grid-cols-3 gap-4 border-t border-slate-100 pt-4 text-sm">
            <div>
              <p class="text-slate-400">상품 금액</p>
              <p class="mt-0.5 font-semibold text-slate-800">{{ order.totalPrice?.toLocaleString() }}원</p>
            </div>
            <div>
              <p class="text-slate-400">할인 금액</p>
              <p class="mt-0.5 font-semibold text-rose-500">- {{ order.discountAmount?.toLocaleString() }}원</p>
            </div>
            <div>
              <p class="text-slate-400">최종 결제</p>
              <p class="mt-0.5 text-lg font-bold text-brand-800">{{ order.finalPrice?.toLocaleString() }}원</p>
            </div>
          </div>
        </section>

        <!-- 주문 상품 목록 -->
        <section class="mb-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="mb-4 text-base font-bold text-slate-800">주문 상품</h2>
          <ul class="divide-y divide-slate-100">
            <li
              v-for="item in order.orderItems"
              :key="item.orderItemId"
              class="flex items-start gap-4 py-3"
            >
              <div class="flex-1 min-w-0">
                <p class="font-medium text-slate-800 line-clamp-1">{{ item.productTitle }}</p>
                <p class="mt-0.5 text-sm text-slate-500">{{ item.productAuthor }}</p>
              </div>
              <div class="shrink-0 text-right">
                <p class="text-sm text-slate-500">{{ item.quantity }}권 × {{ item.salePrice?.toLocaleString() }}원</p>
                <p class="mt-0.5 font-semibold text-slate-800">{{ item.itemTotal?.toLocaleString() }}원</p>
              </div>
            </li>
          </ul>
        </section>

        <!-- 배송 정보 -->
        <section class="mb-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="mb-4 text-base font-bold text-slate-800">배송 정보</h2>
          <dl class="space-y-2 text-sm">
            <div class="flex gap-4">
              <dt class="w-20 shrink-0 text-slate-400">수령인</dt>
              <dd class="text-slate-700">{{ order.receiverName }}</dd>
            </div>
            <div class="flex gap-4">
              <dt class="w-20 shrink-0 text-slate-400">연락처</dt>
              <dd class="text-slate-700">{{ order.receiverPhone }}</dd>
            </div>
            <div class="flex gap-4">
              <dt class="w-20 shrink-0 text-slate-400">주소</dt>
              <dd class="text-slate-700">
                ({{ order.deliveryZipCode }}) {{ order.deliveryAddress }}
                <span v-if="order.deliveryAddressDetail"> {{ order.deliveryAddressDetail }}</span>
              </dd>
            </div>
            <div v-if="order.deliveryMemo" class="flex gap-4">
              <dt class="w-20 shrink-0 text-slate-400">배송 메모</dt>
              <dd class="text-slate-700">{{ order.deliveryMemo }}</dd>
            </div>
          </dl>
        </section>

        <!-- 주문 취소 버튼 -->
        <div
          v-if="order.orderStatus === 'PENDING' || order.orderStatus === 'PAYED'"
          class="flex justify-end"
        >
          <button
            class="rounded-xl bg-rose-500 px-6 py-2.5 font-semibold text-white transition hover:bg-rose-600"
            @click="cancelOrder"
          >
            주문 취소
          </button>
        </div>

      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { memberApi } from '@/api/member'
import { orderApi } from '@/api/order'

const route  = useRoute()
const router = useRouter()

const order   = ref(null)
const loading = ref(false)

// ── 마운트 시 주문 상세 조회 ──────────────────────────────────────────────────
onMounted(async () => {
  loading.value = true
  try {
    const orderId = route.params.orderId
    // GET /api/member/me/orders/{orderId} → 내 주문 상세 (memberId 본인 검증)
    const { data } = await memberApi.getOrder(orderId)
    order.value = data
  } catch (e) {
    console.error('주문 상세 조회 실패', e)
  } finally {
    loading.value = false
  }
})

// ── 주문 취소 ─────────────────────────────────────────────────────────────────
async function cancelOrder() {
  if (!confirm('주문을 취소하시겠습니까?')) return
  try {
    await orderApi.cancel(route.params.orderId, '고객 요청')
    alert('주문이 취소되었습니다.')
    router.push('/mypage/order')
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
