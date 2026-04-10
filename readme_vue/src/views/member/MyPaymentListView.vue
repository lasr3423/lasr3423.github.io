<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="point-chip">결제 내역</p>
          <h1 class="section-title mt-3">최근 결제 상태를 한눈에 확인할 수 있어요</h1>
          <p class="mt-2 text-sm text-slate-500">
            완료, 취소, 실패 상태와 결제 수단을 함께 확인할 수 있습니다.
          </p>
        </div>

        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">전체</p>
            <p class="mt-1 text-lg font-bold text-slate-900">{{ summary.total }}</p>
          </div>
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">완료</p>
            <p class="mt-1 text-lg font-bold text-emerald-600">{{ summary.paid }}</p>
          </div>
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">준비/대기</p>
            <p class="mt-1 text-lg font-bold text-amber-600">{{ summary.ready }}</p>
          </div>
          <div class="surface-soft rounded-2xl px-4 py-3 text-center">
            <p class="text-xs font-semibold text-slate-500">실패/취소</p>
            <p class="mt-1 text-lg font-bold text-rose-600">{{ summary.failed }}</p>
          </div>
        </div>
      </div>
    </section>

    <div v-if="loading" class="surface-panel rounded-[2rem] p-10 text-center text-sm text-slate-500">
      결제 내역을 불러오는 중입니다...
    </div>

    <template v-else-if="payments.length > 0">
      <article
        v-for="payment in payments"
        :key="payment.paymentId"
        class="surface-panel rounded-[2rem] p-5"
      >
        <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div class="space-y-2">
            <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
              {{ formatDate(payment.paidAt || payment.createdAt) }}
            </p>
            <h2 class="text-lg font-semibold text-slate-900">
              주문번호 {{ payment.orderNumber || `#${payment.orderId}` }}
            </h2>
            <div class="flex flex-wrap items-center gap-2 text-sm text-slate-500">
              <span class="rounded-full bg-slate-100 px-3 py-1 font-medium text-slate-600">
                {{ providerLabel(payment.paymentProvider) }}
              </span>
              <span v-if="payment.method" class="rounded-full bg-brand-50 px-3 py-1 font-medium text-brand-700">
                {{ payment.method }}
              </span>
              <span v-if="payment.memberEmail">{{ payment.memberEmail }}</span>
            </div>
          </div>

          <div class="text-left lg:text-right">
            <span
              class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
              :class="statusClass(payment.paymentStatus)"
            >
              {{ statusLabel(payment.paymentStatus) }}
            </span>
            <p class="mt-3 text-2xl font-bold text-brand-800">
              {{ formatPrice(payment.amount) }}원
            </p>
            <p class="mt-1 text-xs text-slate-400">결제 ID {{ payment.paymentId }}</p>
          </div>
        </div>

        <dl class="mt-5 grid gap-3 rounded-2xl bg-slate-50 px-4 py-4 text-sm text-slate-600 md:grid-cols-2">
          <div>
            <dt class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">결제 시각</dt>
            <dd class="mt-1 font-medium text-slate-800">{{ formatDateTime(payment.paidAt || payment.createdAt) }}</dd>
          </div>
          <div>
            <dt class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">PG 거래 정보</dt>
            <dd class="mt-1 break-all font-medium text-slate-800">
              {{ payment.pgTid || payment.paymentKey || '-' }}
            </dd>
          </div>
          <div v-if="payment.cancelledAt">
            <dt class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">취소 시각</dt>
            <dd class="mt-1 font-medium text-slate-800">{{ formatDateTime(payment.cancelledAt) }}</dd>
          </div>
          <div v-if="payment.memberName">
            <dt class="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">주문자</dt>
            <dd class="mt-1 font-medium text-slate-800">{{ payment.memberName }}</dd>
          </div>
        </dl>

        <div
          v-if="payment.failureReason || payment.cancelReason"
          class="mt-4 rounded-2xl border border-rose-100 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {{ payment.failureReason || payment.cancelReason }}
        </div>
      </article>

      <div class="flex items-center justify-center gap-3">
        <button
          class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
          :disabled="page === 0"
          @click="movePage(page - 1)"
        >
          이전
        </button>
        <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
        <button
          class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
          :disabled="page >= totalPages - 1"
          @click="movePage(page + 1)"
        >
          다음
        </button>
      </div>
    </template>

    <div v-else class="surface-panel rounded-[2rem] p-12 text-center">
      <h2 class="text-xl font-semibold text-slate-900">결제 내역이 없습니다</h2>
      <p class="mt-2 text-sm text-slate-500">
        첫 주문을 완료하면 이곳에서 결제 상태를 확인할 수 있습니다.
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { memberApi } from '@/api/member'

const payments = ref([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(1)

const summary = computed(() => {
  const base = { total: payments.value.length, paid: 0, ready: 0, failed: 0 }

  for (const item of payments.value) {
    if (item.paymentStatus === 'PAID') {
      base.paid += 1
    } else if (item.paymentStatus === 'READY') {
      base.ready += 1
    } else if (item.paymentStatus === 'FAILED' || item.paymentStatus === 'CANCELLED') {
      base.failed += 1
    }
  }

  return base
})

function statusLabel(status) {
  return {
    READY: '결제 준비',
    PAID: '결제 완료',
    CANCELLED: '결제 취소',
    FAILED: '결제 실패',
  }[status] || status
}

function statusClass(status) {
  return {
    READY: 'bg-amber-50 text-amber-600',
    PAID: 'bg-emerald-50 text-emerald-600',
    CANCELLED: 'bg-slate-100 text-slate-600',
    FAILED: 'bg-rose-50 text-rose-600',
  }[status] || 'bg-slate-100 text-slate-600'
}

function providerLabel(provider) {
  return {
    TOSS: '토스페이먼츠',
    KAKAO: '카카오페이',
    NAVER: '네이버페이',
  }[provider] || provider || '결제 수단 미정'
}

function formatPrice(value) {
  return Number(value || 0).toLocaleString()
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function fetchPayments() {
  loading.value = true

  try {
    const { data } = await memberApi.getPayments({ page: page.value, size: 10 })
    payments.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (error) {
    console.error('결제 내역 조회 실패', error)
    payments.value = []
  } finally {
    loading.value = false
  }
}

function movePage(nextPage) {
  page.value = nextPage
  fetchPayments()
}

onMounted(fetchPayments)
</script>
