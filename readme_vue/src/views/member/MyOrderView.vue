<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="point-chip">주문 / 배송 조회</p>
          <h1 class="section-title mt-3">최근 주문 상태를 빠르게 확인해 보세요</h1>
          <p class="mt-2 text-sm text-slate-500">
            주문 진행 상태, 결제 금액, 배송 단계까지 한 번에 볼 수 있습니다.
          </p>
        </div>

        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">전체</p>
            <p class="mt-1 text-lg font-bold text-slate-900">{{ summary.total }}</p>
          </div>
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">결제 대기</p>
            <p class="mt-1 text-lg font-bold text-amber-600">{{ summary.pending }}</p>
          </div>
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">배송 중</p>
            <p class="mt-1 text-lg font-bold text-blue-600">{{ summary.shipping }}</p>
          </div>
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">완료/취소</p>
            <p class="mt-1 text-lg font-bold text-emerald-600">{{ summary.done }}</p>
          </div>
        </div>
      </div>
    </section>

    <div v-if="loading" class="surface-panel rounded-[2rem] p-12 text-center text-sm text-slate-500">
      주문 내역을 불러오는 중입니다...
    </div>

    <template v-else-if="orders.length > 0">
      <article
        v-for="order in orders"
        :key="order.orderId"
        class="surface-panel rounded-[2rem] p-5"
      >
        <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
              {{ formatDate(order.orderAt) }}
            </p>
            <h2 class="mt-2 text-xl font-semibold text-slate-900">주문번호 {{ order.number }}</h2>
            <p class="mt-2 text-sm text-slate-500">
              주문 ID #{{ order.orderId }}
            </p>
          </div>

          <div class="text-left lg:text-right">
            <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(order.orderStatus)">
              {{ statusLabel(order.orderStatus) }}
            </span>
            <p class="mt-3 text-2xl font-bold text-brand-800">{{ formatPrice(order.finalPrice) }}원</p>
          </div>
        </div>

        <div class="mt-5 grid gap-3 rounded-2xl bg-slate-50 px-4 py-4 text-sm text-slate-600 md:grid-cols-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">주문일</p>
            <p class="mt-1 font-medium text-slate-800">{{ formatDateTime(order.orderAt) }}</p>
          </div>
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">진행 상태</p>
            <p class="mt-1 font-medium text-slate-800">{{ statusDescription(order.orderStatus) }}</p>
          </div>
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">다음 단계</p>
            <p class="mt-1 font-medium text-slate-800">{{ nextStepLabel(order.orderStatus) }}</p>
          </div>
        </div>

        <div class="mt-5 flex flex-wrap gap-3">
          <button
            class="rounded-xl border border-brand-800 px-4 py-2 text-sm font-semibold text-brand-800 transition hover:bg-brand-50"
            @click="goDetail(order.orderId)"
          >
            주문 상세 보기
          </button>
          <!-- 결제 미완료 주문 → 결제창으로 이동 -->
          <button
            v-if="order.orderStatus === 'PAYMENT_PENDING'"
            class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="resumePayment(order)"
          >
            결제하기
          </button>
          <!-- 배송 완료 → 리뷰 작성 (상세 페이지로 이동) -->
          <button
            v-if="order.orderStatus === 'DELIVERED'"
            class="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-700"
            @click="goDetail(order.orderId)"
          >
            리뷰 작성하기 ★
          </button>
          <button
            v-if="canCancel(order.orderStatus)"
            class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600"
            @click="cancelOrder(order.orderId)"
          >
            주문 취소
          </button>
        </div>
      </article>
    </template>

    <div v-else class="surface-panel rounded-[2rem] p-12 text-center">
      <h2 class="text-xl font-semibold text-slate-900">주문 내역이 없습니다</h2>
      <p class="mt-2 text-sm text-slate-500">
        첫 주문을 완료하면 이곳에서 배송 상태를 확인할 수 있습니다.
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { memberApi } from '@/api/member'
import { orderApi } from '@/api/order'
import { useOrderStore } from '@/store/order'

const router = useRouter()
const orderStore = useOrderStore()
const orders = ref([])
const loading = ref(false)

const summary = computed(() => {
  const base = { total: orders.value.length, pending: 0, shipping: 0, done: 0 }
  for (const order of orders.value) {
    if (['PAYMENT_PENDING', 'PENDING', 'PAYED'].includes(order.orderStatus)) base.pending += 1
    else if (['APPROVAL', 'DELIVERING'].includes(order.orderStatus)) base.shipping += 1
    else if (['DELIVERED', 'CANCELED'].includes(order.orderStatus)) base.done += 1
  }
  return base
})

async function fetchOrders() {
  loading.value = true
  try {
    const { data } = await memberApi.getOrders({ page: 0, size: 20, sort: 'createdAt,desc' })
    orders.value = data.content ?? []
  } catch (error) {
    console.error('주문 목록 조회 실패', error)
  } finally {
    loading.value = false
  }
}

function goDetail(orderId) {
  router.push(`/mypage/order/${orderId}`)
}

function canCancel(status) {
  return status === 'PAYMENT_PENDING' || status === 'PENDING' || status === 'PAYED'
}

// PAYMENT_PENDING 주문 → 결제창 재진입
function resumePayment(order) {
  orderStore.setOrder(
    order.orderId,
    order.finalPrice,
    '주문번호 ' + order.orderNumber,
  )
  router.push('/payment')
}

async function cancelOrder(orderId) {
  if (!confirm('이 주문을 취소하시겠습니까?')) return
  try {
    await orderApi.cancel(orderId, '고객 요청')
    await fetchOrders()
    alert('주문이 취소되었습니다.')
  } catch (error) {
    alert('주문 취소 중 문제가 발생했습니다.')
    console.error(error)
  }
}

function formatPrice(value) {
  return Number(value || 0).toLocaleString()
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

function formatDateTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function statusLabel(status) {
  return {
    PAYMENT_PENDING: '결제 진행 중',
    PENDING:    '승인 대기',
    PAYED:      '승인 대기',
    APPROVAL:   '배송 준비',
    DELIVERING: '배송 중',
    DELIVERED:  '배송 완료',
    CANCELED:   '주문 취소',
  }[status] || status
}

function statusDescription(status) {
  return {
    PAYMENT_PENDING: '결제가 아직 완료되지 않았습니다. 30분 내 결제를 완료해 주세요.',
    PENDING:    '결제가 완료되어 관리자 승인을 기다리고 있습니다.',
    PAYED:      '결제가 완료되어 관리자 승인을 기다리고 있습니다.',
    APPROVAL:   '관리자가 승인했습니다. 곧 배송이 시작됩니다.',
    DELIVERING: '상품이 배송 중입니다.',
    DELIVERED:  '배송이 완료되었습니다.',
    CANCELED:   '주문이 취소되었습니다.',
  }[status] || status
}

function nextStepLabel(status) {
  return {
    PAYMENT_PENDING: '결제하기 버튼으로 결제 진행',
    PENDING:    '관리자 승인 대기',
    PAYED:      '관리자 승인 대기',
    APPROVAL:   '배송 출발 예정',
    DELIVERING: '배송사 배송 중',
    DELIVERED:  '리뷰 작성 가능',
    CANCELED:   '취소 완료',
  }[status] || '-'
}

function statusClass(status) {
  return {
    PAYMENT_PENDING: 'bg-orange-50 text-orange-600',
    PENDING:    'bg-yellow-50 text-yellow-600',
    PAYED:      'bg-yellow-50 text-yellow-600',
    APPROVAL:   'bg-violet-50 text-violet-600',
    DELIVERING: 'bg-sky-50 text-sky-600',
    DELIVERED:  'bg-emerald-50 text-emerald-600',
    CANCELED:   'bg-rose-50 text-rose-600',
  }[status] || 'bg-slate-100 text-slate-500'
}

onMounted(fetchOrders)
</script>
