<template>
  <section class="page-section">
    <div class="mx-auto max-w-5xl">
      <div class="mb-8">
        <p class="point-chip">결제 수단 선택</p>
        <h1 class="section-title mt-3">원하는 방식으로 결제를 진행해 주세요</h1>
        <p class="mt-2 text-sm text-slate-500">
          카카오페이, 네이버페이, 토스 결제 흐름을 한 화면에서 이어서 진행할 수 있습니다.
        </p>
      </div>

      <div
        v-if="!orderStore.orderId"
        class="surface-panel rounded-[2rem] p-10 text-center"
      >
        <h2 class="text-xl font-semibold text-slate-900">결제할 주문이 없습니다</h2>
        <p class="mt-3 text-sm text-slate-500">주문서를 먼저 작성한 뒤 결제를 진행해 주세요.</p>
        <div class="mt-6 flex justify-center gap-3">
          <button
            class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="router.push('/cart')"
          >
            장바구니로 이동
          </button>
          <button
            class="rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="router.push('/order')"
          >
            주문서로 이동
          </button>
        </div>
      </div>

      <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px]">
        <div class="space-y-6">
          <section class="surface-panel rounded-[2rem] p-6">
            <h2 class="text-lg font-semibold text-slate-900">주문 정보</h2>
            <dl class="mt-5 space-y-3 text-sm">
              <div class="flex items-start justify-between gap-4">
                <dt class="text-slate-500">주문 상품</dt>
                <dd class="max-w-[18rem] text-right font-medium text-slate-800">
                  {{ orderStore.itemName || '주문 상품' }}
                </dd>
              </div>
              <div class="flex items-start justify-between gap-4">
                <dt class="text-slate-500">주문 번호</dt>
                <dd class="font-medium text-slate-800">#{{ orderStore.orderId }}</dd>
              </div>
              <div class="flex items-start justify-between gap-4">
                <dt class="text-slate-500">주문자</dt>
                <dd class="font-medium text-slate-800">{{ customerName }}</dd>
              </div>
            </dl>
          </section>

          <section class="surface-panel rounded-[2rem] p-6">
            <div class="mb-5">
              <h2 class="text-lg font-semibold text-slate-900">결제 수단</h2>
              <p class="mt-1 text-sm text-slate-500">결제할 수단을 선택한 뒤 결제창으로 이동합니다.</p>
            </div>

            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <button
                class="rounded-[1.5rem] border p-5 text-left transition"
                :class="selectedProvider === 'TOSS'
                  ? 'border-brand-700 bg-brand-50 shadow-sm'
                  : 'border-slate-200 bg-white hover:border-brand-200 hover:bg-slate-50'"
                @click="selectedProvider = 'TOSS'"
              >
                <div class="flex items-center justify-between">
                  <span class="text-base font-semibold text-slate-900">토스페이먼츠</span>
                  <span class="rounded-full bg-[#e8f1ff] px-3 py-1 text-xs font-semibold text-[#2169f3]">
                    즉시 결제
                  </span>
                </div>
                <p class="mt-3 text-sm leading-6 text-slate-500">
                  카드 결제를 빠르게 진행할 수 있습니다. 현재 SDK 기반으로 연결되어 있습니다.
                </p>
              </button>

              <button
                class="rounded-[1.5rem] border p-5 text-left transition"
                :class="selectedProvider === 'KAKAO'
                  ? 'border-brand-700 bg-brand-50 shadow-sm'
                  : 'border-slate-200 bg-white hover:border-brand-200 hover:bg-slate-50'"
                @click="selectedProvider = 'KAKAO'"
              >
                <div class="flex items-center justify-between">
                  <span class="text-base font-semibold text-slate-900">카카오페이</span>
                  <span class="rounded-full bg-[#fff4bf] px-3 py-1 text-xs font-semibold text-[#765400]">
                    백엔드 연동
                  </span>
                </div>
                <p class="mt-3 text-sm leading-6 text-slate-500">
                  백엔드 결제 준비 API를 호출한 뒤 카카오 결제창으로 이동합니다.
                </p>
              </button>

              <button
                class="rounded-[1.5rem] border p-5 text-left transition"
                :class="selectedProvider === 'NAVER'
                  ? 'border-brand-700 bg-brand-50 shadow-sm'
                  : 'border-slate-200 bg-white hover:border-brand-200 hover:bg-slate-50'"
                @click="selectedProvider = 'NAVER'"
              >
                <div class="flex items-center justify-between">
                  <span class="text-base font-semibold text-slate-900">네이버페이</span>
                  <span class="rounded-full bg-[#dcfce7] px-3 py-1 text-xs font-semibold text-[#127a37]">
                    UX 시작
                  </span>
                </div>
                <p class="mt-3 text-sm leading-6 text-slate-500">
                  네이버페이 SDK를 불러와 팝업 결제 UX를 시작합니다. 현재는 샌드박스 흐름 기준입니다.
                </p>
              </button>
            </div>

            <div class="mt-5 grid gap-3 rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-600 md:grid-cols-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">1단계</p>
                <p class="mt-1 font-medium text-slate-800">결제 수단 선택</p>
              </div>
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">2단계</p>
                <p class="mt-1 font-medium text-slate-800">결제창 이동 및 승인</p>
              </div>
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">3단계</p>
                <p class="mt-1 font-medium text-slate-800">마이페이지 내역 확인</p>
              </div>
            </div>
          </section>
        </div>

        <aside>
          <div class="surface-panel sticky top-28 rounded-[2rem] p-6">
            <h2 class="text-lg font-semibold text-slate-900">최종 결제 금액</h2>
            <p class="mt-4 text-3xl font-bold text-brand-800">
              {{ formatPrice(orderStore.finalPrice) }}원
            </p>
            <p class="mt-2 text-sm text-slate-500">
              선택한 결제 수단으로 안전하게 결제를 진행합니다.
            </p>

            <div class="mt-5 rounded-2xl bg-slate-50 px-4 py-4 text-sm text-slate-600">
              <p class="font-semibold text-slate-800">{{ selectedProviderLabel }}</p>
              <p class="mt-2 leading-6">
                {{
                  selectedProvider === 'KAKAO'
                    ? '결제 준비 API 호출 후 카카오 결제창으로 이동합니다.'
                    : selectedProvider === 'NAVER'
                      ? '네이버페이 SDK를 불러와 팝업 결제창을 띄웁니다.'
                      : '토스 결제 SDK를 통해 카드 결제를 진행합니다.'
                }}
              </p>
            </div>

            <div
              v-if="selectedProvider === 'NAVER'"
              class="mt-4 rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-4 text-sm text-emerald-700"
            >
              샌드박스 기준으로 네이버페이 팝업을 띄웁니다. 팝업 차단이 켜져 있으면 결제가 시작되지 않을 수 있습니다.
            </div>

            <button
              class="mt-6 inline-flex w-full items-center justify-center rounded-xl bg-accent-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-accent-600 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="loading"
              @click="startPayment"
            >
              {{ loading ? '결제창 준비 중...' : `${selectedProviderLabel}로 결제하기` }}
            </button>

            <button
              class="mt-3 inline-flex w-full items-center justify-center rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              :disabled="loading"
              @click="router.push('/order')"
            >
              주문서로 돌아가기
            </button>
          </div>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { memberApi } from '@/api/member'
import { paymentApi } from '@/api/payment'
import { useOrderStore } from '@/store/order'

const router = useRouter()
const orderStore = useOrderStore()

const customerName = ref('주문자')
const customerId = ref(null)
const loading = ref(false)
const selectedProvider = ref('KAKAO')

const selectedProviderLabel = computed(() => (
  selectedProvider.value === 'TOSS'
    ? '토스페이먼츠'
    : selectedProvider.value === 'NAVER'
      ? '네이버페이'
      : '카카오페이'
))

onMounted(async () => {
  try {
    const { data } = await memberApi.getMe()
    customerName.value = data.name || data.email || '주문자'
    customerId.value = data.id || null
  } catch (error) {
    console.error('회원 정보 조회 실패', error)
  }
})

function formatPrice(value) {
  return Number(value || 0).toLocaleString()
}

function buildTossOrderId(orderId) {
  return `ORDER-${String(orderId).padStart(10, '0')}`
}

function persistPaymentMeta(orderId, meta) {
  sessionStorage.setItem(`payment-meta-${orderId}`, JSON.stringify(meta))
}

function loadNaverPaySdk() {
  if (window.Naver?.Pay) {
    return Promise.resolve(window.Naver.Pay)
  }

  return new Promise((resolve, reject) => {
    const existing = document.querySelector('script[data-naver-pay-sdk="true"]')

    if (existing) {
      existing.addEventListener('load', () => resolve(window.Naver?.Pay))
      existing.addEventListener('error', () => reject(new Error('네이버페이 SDK 로드에 실패했습니다.')))
      return
    }

    const script = document.createElement('script')
    script.src = 'https://nsp.pay.naver.com/sdk/js/naverpay.min.js'
    script.async = true
    script.dataset.naverPaySdk = 'true'
    script.onload = () => resolve(window.Naver?.Pay)
    script.onerror = () => reject(new Error('네이버페이 SDK 로드에 실패했습니다.'))
    document.head.appendChild(script)
  })
}

async function startPayment() {
  if (!orderStore.orderId || loading.value) {
    return
  }

  if (selectedProvider.value === 'TOSS') {
    requestTossPayment()
    return
  }

  if (selectedProvider.value === 'NAVER') {
    await requestNaverPayment()
    return
  }

  loading.value = true

  try {
    const origin = window.location.origin
    const orderId = orderStore.orderId
    const provider = 'KAKAO'

    const { data } = await paymentApi.ready({
      orderId,
      provider,
      itemName: orderStore.itemName || '도서 주문',
      amount: orderStore.finalPrice,
      approvalUrl: `${origin}/payment/success?provider=KAKAO&orderId=${orderId}`,
      cancelUrl: `${origin}/payment/fail?provider=KAKAO&orderId=${orderId}&code=CANCELED&message=${encodeURIComponent('결제가 취소되었습니다.')}`,
      failUrl: `${origin}/payment/fail?provider=KAKAO&orderId=${orderId}&code=FAILED&message=${encodeURIComponent('결제 승인에 실패했습니다.')}`,
    })

    persistPaymentMeta(orderId, {
      provider,
      tid: data.tid || null,
      amount: orderStore.finalPrice,
    })

    const redirectUrl = data.redirectPcUrl || data.redirectMobileUrl || data.redirectUrl

    if (!redirectUrl) {
      throw new Error('결제창 이동 주소를 받지 못했습니다.')
    }

    window.location.href = redirectUrl
  } catch (error) {
    console.error('카카오 결제 준비 실패', error)
    alert('카카오페이 결제를 준비하는 중 문제가 발생했습니다. 관리자 키와 CID 설정을 확인해 주세요.')
  } finally {
    loading.value = false
  }
}

async function requestNaverPayment() {
  loading.value = true

  try {
    const origin = window.location.origin
    const orderId = orderStore.orderId

    const { data } = await paymentApi.ready({
      orderId,
      provider: 'NAVER',
      itemName: orderStore.itemName || '도서 주문',
      amount: orderStore.finalPrice,
      approvalUrl: `${origin}/payment/success?provider=NAVER&orderId=${orderId}`,
      cancelUrl: `${origin}/payment/fail?provider=NAVER&orderId=${orderId}&code=CANCELED&message=${encodeURIComponent('결제가 취소되었습니다.')}`,
      failUrl: `${origin}/payment/fail?provider=NAVER&orderId=${orderId}&code=FAILED&message=${encodeURIComponent('결제 승인에 실패했습니다.')}`,
    })

    persistPaymentMeta(orderId, {
      provider: 'NAVER',
      paymentId: data.paymentId || String(orderId),
      amount: orderStore.finalPrice,
    })

    const naverPay = await loadNaverPaySdk()

    if (!naverPay?.create) {
      throw new Error('네이버페이 SDK 객체를 찾지 못했습니다.')
    }

    const oPay = naverPay.create({
      mode: 'development',
      clientId: 'HN3GGCMDdTgGUfl0kFCo',
      chainId: 'Wmhwa0NxNE16eFh',
      openType: 'popup',
    })

    oPay.open({
      merchantPayKey: String(data.paymentId || orderId),
      merchantUserKey: String(customerId.value || orderId),
      productName: orderStore.itemName || '도서 주문',
      totalPayAmount: Number(orderStore.finalPrice),
      taxScopeAmount: Number(orderStore.finalPrice),
      taxExScopeAmount: 0,
      returnUrl: `${origin}/payment/success?provider=NAVER&orderId=${orderId}`,
    })
  } catch (error) {
    console.error('네이버페이 시작 실패', error)
    alert('네이버페이 결제를 시작하는 중 문제가 발생했습니다. 팝업 차단 여부와 샌드박스 설정을 확인해 주세요.')
  } finally {
    loading.value = false
  }
}

function requestTossPayment() {
  const clientKey = 'test_ck_AQ92ymxN34vmXlbmObdPrajRKXvd'
  const tossPayments = window.TossPayments(clientKey)
  const tossOrderId = buildTossOrderId(orderStore.orderId)

  persistPaymentMeta(orderStore.orderId, {
    provider: 'TOSS',
    amount: orderStore.finalPrice,
  })

  tossPayments.requestPayment('카드', {
    amount: orderStore.finalPrice,
    orderId: tossOrderId,
    orderName: orderStore.itemName || '도서 주문',
    customerName: customerName.value,
    successUrl: `${window.location.origin}/payment/success?provider=TOSS`,
    failUrl: `${window.location.origin}/payment/fail?provider=TOSS&orderId=${orderStore.orderId}`,
  })
}
</script>
