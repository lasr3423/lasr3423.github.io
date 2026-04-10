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

        <div class="mt-8 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-left">
          <p class="text-xs font-semibold text-slate-500">오류 코드</p>
          <p class="mt-1 text-sm font-medium text-slate-800">{{ failureCode }}</p>
        </div>

        <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <button
            class="rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="router.push('/payment')"
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
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paymentApi } from '@/api/payment'

const route = useRoute()
const router = useRouter()

const failureCode = computed(() => String(route.query.code || 'UNKNOWN'))
const failureMessage = computed(() => String(route.query.message || '결제 처리 중 문제가 발생했습니다.'))

onMounted(async () => {
  const orderId = Number(String(route.query.orderId || '').replace('ORDER-', ''))

  if (!orderId) {
    return
  }

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
