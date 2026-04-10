<template>
  <section class="page-section">
    <div class="mx-auto max-w-xl">
      <div class="surface-panel rounded-[2rem] p-10 text-center">

        <!-- 성공 아이콘 -->
        <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-2xl text-emerald-600">
          ✓
        </div>
        <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-600">결제 완료</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">
          결제가 성공적으로 완료되었습니다
        </h1>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          주문 내역은 마이페이지에서 확인할 수 있습니다.
        </p>

        <!-- 결제 정보 박스 -->
        <div class="mt-8 rounded-2xl border border-slate-200 bg-slate-50 p-5 text-left space-y-3">
          <div class="flex items-start justify-between gap-4 text-sm">
            <span class="shrink-0 text-slate-500">주문 번호</span>
            <span class="font-medium text-slate-800">#{{ orderId }}</span>
          </div>
          <div class="flex items-start justify-between gap-4 text-sm">
            <span class="shrink-0 text-slate-500">상품명</span>
            <span class="font-medium text-slate-800 text-right">{{ itemName }}</span>
          </div>
          <div class="flex items-start justify-between gap-4 text-sm">
            <span class="shrink-0 text-slate-500">결제 수단</span>
            <span class="font-medium text-slate-800">{{ providerLabel }}</span>
          </div>
          <div class="my-1 border-t border-slate-200" />
          <div class="flex items-center justify-between gap-4 text-sm">
            <span class="shrink-0 text-slate-500">결제 금액</span>
            <span class="text-xl font-bold text-brand-800">{{ formatPrice(amount) }}원</span>
          </div>
          <div class="flex items-start justify-between gap-4 text-sm">
            <span class="shrink-0 text-slate-500">승인 시각</span>
            <span class="font-medium text-slate-800">{{ formattedApprovedAt }}</span>
          </div>
        </div>

        <!-- 액션 버튼 -->
        <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <button
            class="rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="router.push('/mypage/order')"
          >
            주문 내역 보기
          </button>
          <button
            class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="router.push('/')"
          >
            쇼핑 계속하기
          </button>
        </div>

      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const orderId    = computed(() => route.query.orderId   || '')
const amount     = computed(() => Number(route.query.amount || 0))
const itemName   = computed(() => String(route.query.itemName  || '도서 주문'))
const provider   = computed(() => String(route.query.provider  || 'TOSS').toUpperCase())
const approvedAt = computed(() => String(route.query.approvedAt || ''))

const providerLabel = computed(() => {
  switch (provider.value) {
    case 'TOSS':  return '토스페이먼츠'
    case 'KAKAO': return '카카오페이'
    case 'NAVER': return '네이버페이'
    default:      return provider.value
  }
})

const formattedApprovedAt = computed(() => {
  if (!approvedAt.value) return '-'
  try {
    return new Date(approvedAt.value).toLocaleString('ko-KR', {
      year:   'numeric',
      month:  '2-digit',
      day:    '2-digit',
      hour:   '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  } catch {
    return approvedAt.value
  }
})

function formatPrice(value) {
  return Number(value || 0).toLocaleString()
}
</script>
