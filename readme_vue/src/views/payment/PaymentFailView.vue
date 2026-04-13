<template>
  <section class="page-section">
    <div class="mx-auto max-w-xl">
      <div class="surface-panel rounded-[2rem] p-10 text-center">
        <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-rose-100 text-2xl text-rose-600">
          !
        </div>
        <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-rose-600">결제 실패</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">
          결제가 완료되지 않았습니다
        </h1>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          {{ failureMessage }}
        </p>

        <div class="mt-8 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-left space-y-2">
          <div class="flex items-center justify-between text-sm">
            <span class="text-slate-500">오류 코드</span>
            <span class="font-medium text-slate-800">{{ failureCode }}</span>
          </div>
          <div v-if="failureOrderId" class="flex items-center justify-between text-sm">
            <span class="text-slate-500">주문 번호</span>
            <span class="font-medium text-slate-800">#{{ failureOrderId }}</span>
          </div>
          <div v-if="providerLabel" class="flex items-center justify-between text-sm">
            <span class="text-slate-500">결제 수단</span>
            <span class="font-medium text-slate-800">{{ providerLabel }}</span>
          </div>
        </div>

        <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <!-- 주문 재시도: orderStore가 살아있으면 /payment, 아니면 /order로 이동 -->
          <button
            class="rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="retryPayment"
          >
            다시 결제하기
          </button>
          <button
            class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="router.push('/cart')"
          >
            장바구니로 이동
          </button>
        </div>

        <p class="mt-6 text-xs text-slate-400">
          문제가 반복되면 다른 결제 수단을 이용하거나 고객센터로 문의해 주세요.
        </p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paymentApi } from '@/api/payment'
import { useOrderStore } from '@/store/order'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const failureCode    = computed(() => String(route.query.code    || 'UNKNOWN'))
const failureMessage = computed(() => String(route.query.message || '결제 처리 중 문제가 발생했습니다.'))
const failureOrderId = computed(() => {
  const raw = String(route.query.orderId || '')
  return raw ? raw.replace('ORDER-', '') : ''
})

const providerLabel = computed(() => {
  switch (String(route.query.provider || '').toUpperCase()) {
    case 'KAKAO': return '카카오페이'
    case 'NAVER': return '네이버페이'
    case 'TOSS':  return '토스페이'
    case 'BANK_TRANSFER': return '계좌이체'
    default: return ''
  }
})

// 다시 결제하기: orderStore에 주문 정보가 남아있으면 /payment, 없으면 /order로
function retryPayment() {
  if (orderStore.orderId) {
    router.push('/payment')
  } else {
    router.push('/order')
  }
}

onMounted(async () => {
  const code = failureCode.value

  // 사용자 취소 → 장바구니로 즉시 이동
  if (code === 'CANCELED' || code === 'PAY_PROCESS_CANCELED' || code === 'USER_CANCEL') {
    router.replace('/cart')
    return
  }

  const orderId = Number(String(route.query.orderId || '').replace('ORDER-', ''))
  if (!orderId) return

  try {
    await paymentApi.fail({
      orderId,
      code: failureCode.value,
      message: failureMessage.value,
    })
  } catch (error) {
    console.error('결제 실패 후처리 실패', error)
  }
})
</script>
