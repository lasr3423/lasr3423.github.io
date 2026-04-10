<template>
  <section class="page-section">
    <div class="mx-auto max-w-6xl">
      <div class="mb-8">
        <p class="point-chip">주문서 작성</p>
        <h1 class="section-title mt-3">배송 정보와 결제 금액을 확인해 주세요</h1>
        <p class="mt-2 text-sm text-slate-500">
          장바구니에서 선택한 상품을 기준으로 주문이 생성됩니다.
        </p>
      </div>

      <div
        v-if="checkedItems.length === 0"
        class="surface-panel rounded-[2rem] p-10 text-center"
      >
        <h2 class="text-xl font-semibold text-slate-900">선택한 상품이 없습니다</h2>
        <p class="mt-3 text-sm text-slate-500">
          장바구니에서 주문할 상품을 먼저 선택해 주세요.
        </p>
        <button
          class="mt-6 inline-flex rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="router.push('/cart')"
        >
          장바구니로 이동
        </button>
      </div>

      <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div class="space-y-6">
          <section class="surface-panel rounded-[2rem] p-6">
            <div class="mb-4 flex items-center justify-between">
              <h2 class="text-lg font-semibold text-slate-900">주문 상품</h2>
              <span class="text-sm text-slate-500">{{ checkedItems.length }}건</span>
            </div>

            <ul class="divide-y divide-slate-100">
              <li
                v-for="item in checkedItems"
                :key="item.cartItemId"
                class="flex items-start justify-between gap-4 py-4"
              >
                <div class="min-w-0">
                  <p class="truncate text-base font-semibold text-slate-900">{{ item.title }}</p>
                  <p class="mt-1 text-sm text-slate-500">{{ item.author }}</p>
                </div>
                <div class="shrink-0 text-right">
                  <p class="text-sm text-slate-500">{{ item.quantity }}권</p>
                  <p class="mt-1 font-semibold text-slate-900">
                    {{ formatPrice(item.itemTotal) }}원
                  </p>
                </div>
              </li>
            </ul>
          </section>

          <section class="surface-panel rounded-[2rem] p-6">
            <div class="mb-5">
              <h2 class="text-lg font-semibold text-slate-900">배송 정보</h2>
              <p class="mt-1 text-sm text-slate-500">실제 수령하실 정보를 입력해 주세요.</p>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <label class="space-y-2">
                <span class="text-sm font-medium text-slate-700">수령인</span>
                <input
                  v-model.trim="form.receiverName"
                  type="text"
                  placeholder="예: 홍길동"
                  class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-700 focus:ring-4 focus:ring-brand-100"
                />
              </label>

              <label class="space-y-2">
                <span class="text-sm font-medium text-slate-700">연락처</span>
                <input
                  v-model.trim="form.receiverPhone"
                  type="tel"
                  placeholder="예: 010-1234-5678"
                  class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-700 focus:ring-4 focus:ring-brand-100"
                />
              </label>

              <label class="space-y-2 md:col-span-2">
                <span class="text-sm font-medium text-slate-700">우편번호</span>
                <input
                  v-model.trim="form.deliveryZipCode"
                  type="text"
                  maxlength="5"
                  placeholder="예: 12345"
                  class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-700 focus:ring-4 focus:ring-brand-100"
                />
              </label>

              <label class="space-y-2 md:col-span-2">
                <span class="text-sm font-medium text-slate-700">주소</span>
                <input
                  v-model.trim="form.deliveryAddress"
                  type="text"
                  placeholder="기본 주소를 입력해 주세요"
                  class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-700 focus:ring-4 focus:ring-brand-100"
                />
              </label>

              <label class="space-y-2 md:col-span-2">
                <span class="text-sm font-medium text-slate-700">상세 주소</span>
                <input
                  v-model.trim="form.deliveryAddressDetail"
                  type="text"
                  placeholder="상세 주소를 입력해 주세요"
                  class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-700 focus:ring-4 focus:ring-brand-100"
                />
              </label>

              <label class="space-y-2 md:col-span-2">
                <span class="text-sm font-medium text-slate-700">배송 메모</span>
                <input
                  v-model.trim="form.deliveryMemo"
                  type="text"
                  placeholder="예: 문 앞에 놓아 주세요"
                  class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-700 focus:ring-4 focus:ring-brand-100"
                />
              </label>
            </div>
          </section>
        </div>

        <aside class="xl:pt-1">
          <div class="surface-panel sticky top-28 rounded-[2rem] p-6">
            <h2 class="text-lg font-semibold text-slate-900">결제 금액</h2>

            <dl class="mt-5 space-y-3 text-sm">
              <div class="flex items-center justify-between text-slate-500">
                <dt>상품 금액</dt>
                <dd>{{ formatPrice(cartStore.checkedTotal) }}원</dd>
              </div>
              <div class="flex items-center justify-between text-slate-500">
                <dt>배송비</dt>
                <dd>무료</dd>
              </div>
              <div class="border-t border-slate-100 pt-3">
                <div class="flex items-center justify-between text-base font-semibold text-slate-900">
                  <dt>최종 결제 금액</dt>
                  <dd class="text-brand-800">{{ formatPrice(cartStore.checkedTotal) }}원</dd>
                </div>
              </div>
            </dl>

            <button
              class="mt-6 inline-flex w-full items-center justify-center rounded-xl bg-accent-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-accent-600 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="submitting || !isFormValid"
              @click="submitOrder"
            >
              {{ submitting ? '주문 생성 중...' : '주문 생성 후 결제하기' }}
            </button>

            <p v-if="!isFormValid" class="mt-3 text-center text-xs text-rose-500">
              필수 항목을 모두 입력해야 주문을 진행할 수 있습니다.
            </p>
          </div>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { useOrderStore } from '@/store/order'
import { orderApi } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()
const orderStore = useOrderStore()

const form = ref({
  receiverName: '',
  receiverPhone: '',
  deliveryZipCode: '',
  deliveryAddress: '',
  deliveryAddressDetail: '',
  deliveryMemo: '',
})

const submitting = ref(false)

const checkedItems = computed(() => cartStore.items.filter((item) => item.isChecked))

const isFormValid = computed(() =>
  form.value.receiverName &&
  form.value.receiverPhone &&
  form.value.deliveryZipCode &&
  form.value.deliveryAddress,
)

onMounted(async () => {
  if (!cartStore.items.length) {
    await cartStore.fetchCart()
  }
})

function formatPrice(value) {
  return Number(value || 0).toLocaleString()
}

async function submitOrder() {
  if (!isFormValid.value || submitting.value) {
    return
  }

  submitting.value = true

  try {
    const { data } = await orderApi.create({
      cartItemIds: checkedItems.value.map((item) => item.cartItemId),
      receiverName: form.value.receiverName,
      receiverPhone: form.value.receiverPhone,
      deliveryZipCode: form.value.deliveryZipCode,
      deliveryAddress: form.value.deliveryAddress,
      deliveryAddressDetail: form.value.deliveryAddressDetail,
      deliveryMemo: form.value.deliveryMemo,
    })

    orderStore.setOrder(data.orderId, data.finalPrice, data.itemName)
    router.push('/payment')
  } catch (error) {
    console.error('주문 생성 실패', error)
    alert('주문 생성 중 문제가 발생했습니다. 입력 정보를 다시 확인해 주세요.')
  } finally {
    submitting.value = false
  }
}
</script>
