<template>
  <div class="min-h-screen bg-slate-50 py-8">
    <div class="mx-auto max-w-xl px-4">

      <h1 class="mb-6 text-2xl font-bold text-brand-800">결제</h1>

      <!-- 주문 정보 없을 때 -->
      <div
        v-if="!orderStore.orderId"
        class="rounded-2xl border border-slate-200 bg-white py-16 text-center text-slate-400"
      >
        결제할 주문 정보가 없습니다.
        <div class="mt-4">
          <button
            class="rounded-xl bg-brand-800 px-6 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="router.push('/cart')"
          >
            장바구니로 이동
          </button>
        </div>
      </div>

      <template v-else>

        <!-- 결제 요약 카드 -->
        <section class="mb-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="mb-3 text-base font-bold text-slate-800">주문 정보</h2>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between">
              <dt class="text-slate-500">상품명</dt>
              <dd class="max-w-48 truncate font-medium text-slate-700 text-right">{{ orderStore.itemName }}</dd>
            </div>
            <div class="my-2 border-t border-slate-100" />
            <div class="flex justify-between text-base font-bold">
              <dt class="text-slate-700">결제 금액</dt>
              <dd class="text-brand-800">{{ orderStore.finalPrice.toLocaleString() }}원</dd>
            </div>
          </dl>
        </section>

        <!-- 결제 버튼 -->
        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="mb-4 text-base font-bold text-slate-800">결제 수단</h2>

          <button
            class="flex w-full items-center justify-center gap-3 rounded-xl bg-[#3182F6] py-3.5 font-bold text-white transition hover:bg-[#1b64da] disabled:opacity-50"
            :disabled="loading || !customerName"
            @click="payWithToss"
          >
            <span v-if="loading">로딩 중...</span>
            <template v-else>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z" fill="white" opacity="0.3"/>
                <path d="M12 6v6l4 2" stroke="white" stroke-width="2" stroke-linecap="round"/>
              </svg>
              토스페이먼츠로 결제하기
            </template>
          </button>

          <p class="mt-4 text-center text-xs text-slate-400">
            결제 버튼 클릭 시 토스페이먼츠 결제창이 열립니다.
          </p>
        </section>

      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/store/order'
import { memberApi } from '@/api/member'

const router     = useRouter()
const orderStore = useOrderStore()

const customerName = ref('')
const loading      = ref(false)

// ── 마운트 시 회원 이름 조회 ──────────────────────────────────────────────────
onMounted(async () => {
  loading.value = true
  try {
    const { data } = await memberApi.getMe()
    customerName.value = data.name ?? data.email ?? '구매자'
  } catch (e) {
    customerName.value = '구매자'
    console.error('회원 정보 조회 실패', e)
  } finally {
    loading.value = false
  }
})

// ── 토스페이먼츠 결제 요청 ────────────────────────────────────────────────────
function payWithToss() {
  // 공개 클라이언트 키 (노출 가능)
  const clientKey   = 'test_ck_AQ92ymxN34vmXlbmObdPrajRKXvd'
  const tossPayments = window.TossPayments(clientKey)

  // Toss orderId 규칙: 영문·숫자·(-,_) 조합, 6~64자
  // DB PK 단독("1","2")은 길이 미달 → ORDER-0000000001 형식으로 변환
  const tossOrderId = `ORDER-${String(orderStore.orderId).padStart(10, '0')}`

  tossPayments.requestPayment('카드', {
    amount:       orderStore.finalPrice,
    orderId:      tossOrderId,
    orderName:    orderStore.itemName || '도서 주문',
    customerName: customerName.value,
    successUrl:   `${window.location.origin}/payment/success`,
    failUrl:      `${window.location.origin}/payment/fail`,
  })
}
</script>
