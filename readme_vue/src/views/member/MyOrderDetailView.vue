<template>
  <div class="space-y-6">
    <button
      class="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-brand-800"
      @click="router.push('/mypage/order')"
    >
      주문 목록으로 돌아가기
    </button>

    <div v-if="loading" class="surface-panel rounded-[2rem] p-12 text-center text-sm text-slate-500">
      주문 정보를 불러오는 중입니다...
    </div>

    <div v-else-if="!order" class="surface-panel rounded-[2rem] p-12 text-center">
      <h2 class="text-xl font-semibold text-slate-900">주문 정보를 찾을 수 없습니다</h2>
      <p class="mt-2 text-sm text-slate-500">잠시 후 다시 시도해 주세요.</p>
    </div>

    <template v-else>
      <section class="surface-panel rounded-[2rem] p-6">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <p class="point-chip">주문 상세</p>
            <h1 class="section-title mt-3">주문번호 {{ order.number }}</h1>
            <p class="mt-2 text-sm text-slate-500">{{ formatDateTime(order.orderAt) }}</p>
          </div>
          <div class="text-left lg:text-right">
            <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(order.orderStatus)">
              {{ statusLabel(order.orderStatus) }}
            </span>
            <p class="mt-3 text-3xl font-bold text-brand-800">{{ formatPrice(order.finalPrice) }}원</p>
          </div>
        </div>

        <div class="mt-6 grid gap-4 md:grid-cols-3">
          <div class="surface-soft rounded-2xl p-4">
            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">상품 금액</p>
            <p class="mt-2 text-lg font-semibold text-slate-900">{{ formatPrice(order.totalPrice) }}원</p>
          </div>
          <div class="surface-soft rounded-2xl p-4">
            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">할인 금액</p>
            <p class="mt-2 text-lg font-semibold text-rose-500">-{{ formatPrice(order.discountAmount) }}원</p>
          </div>
          <div class="surface-soft rounded-2xl p-4">
            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">최종 결제</p>
            <p class="mt-2 text-lg font-semibold text-brand-800">{{ formatPrice(order.finalPrice) }}원</p>
          </div>
        </div>
      </section>

      <section class="surface-panel rounded-[2rem] p-6">
        <div class="mb-4">
          <h2 class="text-xl font-bold text-slate-900">주문 상품</h2>
          <p class="mt-1 text-sm text-slate-500">총 {{ order.orderItems.length }}개 상품</p>
        </div>

        <ul class="divide-y divide-slate-100">
          <li
            v-for="item in order.orderItems"
            :key="item.orderItemId"
            class="flex flex-col gap-4 py-4 sm:flex-row sm:items-start sm:justify-between"
          >
            <div>
              <p class="text-base font-semibold text-slate-900">{{ item.productTitle }}</p>
              <p class="mt-1 text-sm text-slate-500">{{ item.productAuthor }}</p>
            </div>
            <div class="text-left sm:text-right">
              <p class="text-sm text-slate-500">{{ item.quantity }}권 x {{ formatPrice(item.salePrice) }}원</p>
              <p class="mt-1 font-semibold text-slate-900">{{ formatPrice(item.itemTotal) }}원</p>
            </div>
          </li>
        </ul>
      </section>

      <section class="grid gap-6 lg:grid-cols-2">
        <article class="surface-panel rounded-[2rem] p-6">
          <h2 class="text-xl font-bold text-slate-900">배송 정보</h2>
          <dl class="mt-5 space-y-4 text-sm">
            <div class="grid grid-cols-[88px_minmax(0,1fr)] gap-3">
              <dt class="font-semibold text-slate-400">수령인</dt>
              <dd class="text-slate-800">{{ order.receiverName }}</dd>
            </div>
            <div class="grid grid-cols-[88px_minmax(0,1fr)] gap-3">
              <dt class="font-semibold text-slate-400">연락처</dt>
              <dd class="text-slate-800">{{ order.receiverPhone }}</dd>
            </div>
            <div class="grid grid-cols-[88px_minmax(0,1fr)] gap-3">
              <dt class="font-semibold text-slate-400">주소</dt>
              <dd class="text-slate-800">
                ({{ order.deliveryZipCode }}) {{ order.deliveryAddress }}
                <span v-if="order.deliveryAddressDetail"> {{ order.deliveryAddressDetail }}</span>
              </dd>
            </div>
            <div v-if="order.deliveryMemo" class="grid grid-cols-[88px_minmax(0,1fr)] gap-3">
              <dt class="font-semibold text-slate-400">배송 메모</dt>
              <dd class="text-slate-800">{{ order.deliveryMemo }}</dd>
            </div>
          </dl>
        </article>

        <article class="surface-panel rounded-[2rem] p-6">
          <h2 class="text-xl font-bold text-slate-900">진행 상태 안내</h2>
          <div class="mt-5 space-y-4">
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">현재 상태</p>
              <p class="mt-2 text-lg font-semibold text-slate-900">{{ statusLabel(order.orderStatus) }}</p>
              <p class="mt-2 text-sm leading-6 text-slate-500">{{ statusDescription(order.orderStatus) }}</p>
            </div>
            <div v-if="order.cancelledAt" class="rounded-2xl border border-rose-100 bg-rose-50 p-4 text-sm text-rose-700">
              취소 처리 시각: {{ formatDateTime(order.cancelledAt) }}
            </div>
          </div>
        </article>
      </section>

      <div v-if="canCancel(order.orderStatus)" class="flex justify-end">
        <button
          class="rounded-xl bg-rose-500 px-6 py-3 text-sm font-semibold text-white transition hover:bg-rose-600"
          @click="cancelOrder"
        >
          주문 취소
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { memberApi } from '@/api/member'
import { orderApi } from '@/api/order'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(false)

async function fetchOrder() {
  loading.value = true
  try {
    const { data } = await memberApi.getOrder(route.params.orderId)
    order.value = data
  } catch (error) {
    console.error('주문 상세 조회 실패', error)
    order.value = null
  } finally {
    loading.value = false
  }
}

function canCancel(status) {
  return status === 'PENDING' || status === 'PAYED'
}

async function cancelOrder() {
  if (!confirm('이 주문을 취소하시겠습니까?')) return
  try {
    await orderApi.cancel(route.params.orderId, '고객 요청')
    alert('주문이 취소되었습니다.')
    router.push('/mypage/order')
  } catch (error) {
    alert('주문 취소 중 문제가 발생했습니다.')
    console.error(error)
  }
}

function formatPrice(value) {
  return Number(value || 0).toLocaleString()
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
    PENDING: '결제 대기',
    PAYED: '결제 완료',
    APPROVAL: '배송 준비',
    DELIVERING: '배송 중',
    DELIVERED: '배송 완료',
    CANCELED: '주문 취소',
  }[status] || status
}

function statusDescription(status) {
  return {
    PENDING: '결제를 진행하기 전 상태입니다.',
    PAYED: '결제가 완료되어 출고 승인을 기다리고 있습니다.',
    APPROVAL: '상품 출고 준비가 진행 중입니다.',
    DELIVERING: '택배사에서 배송이 진행 중입니다.',
    DELIVERED: '상품이 정상적으로 배송 완료되었습니다.',
    CANCELED: '주문이 취소되어 배송이 진행되지 않습니다.',
  }[status] || status
}

function statusClass(status) {
  return {
    PENDING: 'bg-amber-50 text-amber-600',
    PAYED: 'bg-blue-50 text-blue-600',
    APPROVAL: 'bg-violet-50 text-violet-600',
    DELIVERING: 'bg-sky-50 text-sky-600',
    DELIVERED: 'bg-emerald-50 text-emerald-600',
    CANCELED: 'bg-rose-50 text-rose-600',
  }[status] || 'bg-slate-100 text-slate-500'
}

onMounted(fetchOrder)
</script>
