<template>
  <section class="page-section">
    <div class="mx-auto max-w-xl">
      <div class="surface-panel rounded-[2rem] p-10 text-center">
        <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-2xl text-emerald-600">
          ✓
        </div>
        <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-600">결제 완료</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">
          결제가 정상적으로 완료되었습니다
        </h1>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          주문과 결제 상태를 모두 반영한 뒤 상세 내역을 확인할 수 있어요.
        </p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paymentApi } from '@/api/payment'
import { useOrderStore } from '@/store/order'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

function readPaymentMeta(orderId) {
  const raw = sessionStorage.getItem(`payment-meta-${orderId}`)
  return raw ? JSON.parse(raw) : null
}

function clearPaymentMeta(orderId) {
  sessionStorage.removeItem(`payment-meta-${orderId}`)
}

onMounted(async () => {
  const provider = String(route.query.provider || 'TOSS').toUpperCase()

  try {
    if (provider === 'KAKAO') {
      const orderId = Number(route.query.orderId)
      const pgToken = String(route.query.pg_token || '')
      const meta = readPaymentMeta(orderId)

      if (!orderId || !pgToken || !meta?.tid) {
        throw new Error('카카오 결제 승인 정보가 부족합니다.')
      }

      await paymentApi.approve({
        orderId,
        provider: 'KAKAO',
        tid: meta.tid,
        pgToken,
      })

      clearPaymentMeta(orderId)
      orderStore.clearOrder()
      router.replace({
        path: '/payment/complete',
        query: {
          orderId,
          amount: meta.amount,
          itemName: meta.itemName || '도서 주문',
          provider: 'KAKAO',
          approvedAt: new Date().toISOString(),
        },
      })
      return
    }

    if (provider === 'NAVER') {
      const orderId = Number(route.query.orderId)
      const resultCode = String(route.query.resultCode || '')
      const paymentId = String(route.query.paymentId || '')
      const meta = readPaymentMeta(orderId)

      if (resultCode && resultCode !== 'Success') {
        throw new Error('네이버페이 승인 결과가 성공이 아닙니다.')
      }

      if (!orderId || !(paymentId || meta?.paymentId)) {
        throw new Error('네이버페이 승인 정보가 부족합니다.')
      }

      await paymentApi.approve({
        orderId,
        provider: 'NAVER',
        paymentId: paymentId || meta.paymentId,
        resultCode: resultCode || 'Success',
      })

      clearPaymentMeta(orderId)
      orderStore.clearOrder()
      router.replace({
        path: '/payment/complete',
        query: {
          orderId,
          amount: meta?.amount,
          itemName: meta?.itemName || '도서 주문',
          provider: 'NAVER',
          approvedAt: new Date().toISOString(),
        },
      })
      return
    }

    const paymentKey = String(route.query.paymentKey || '')
    const amount = Number(route.query.amount || 0)
    const orderIdText = String(route.query.orderId || '')
    const numericOrderId = Number(orderIdText.replace('ORDER-', ''))

    if (!paymentKey || !numericOrderId || !amount) {
      throw new Error('토스 결제 승인 정보가 부족합니다.')
    }

    const tossMeta = readPaymentMeta(numericOrderId)

    await paymentApi.confirm({
      paymentKey,
      orderId: numericOrderId,
      amount,
    })

    clearPaymentMeta(numericOrderId)
    orderStore.clearOrder()
    router.replace({
      path: '/payment/complete',
      query: {
        orderId: numericOrderId,
        amount,
        itemName: tossMeta?.itemName || '도서 주문',
        provider: 'TOSS',
        approvedAt: new Date().toISOString(),
      },
    })
  } catch (error) {
    console.error('결제 승인 실패', error)
    const orderId = route.query.orderId ? String(route.query.orderId) : ''
    router.replace({
      path: '/payment/fail',
      query: {
        provider,
        orderId,
        code: 'CONFIRM_FAILED',
        message: '결제 승인 처리 중 문제가 발생했습니다.',
      },
    })
  }
})
</script>
