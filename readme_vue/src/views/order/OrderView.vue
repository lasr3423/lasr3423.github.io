<template>
  <div class="min-h-screen bg-slate-50 py-8">
    <div class="mx-auto max-w-3xl px-4">

      <h1 class="mb-6 text-2xl font-bold text-brand-800">주문서</h1>

      <!-- 주문 상품 없을 때 -->
      <div
        v-if="checkedItems.length === 0"
        class="rounded-2xl border border-slate-200 bg-white py-16 text-center text-slate-400"
      >
        선택된 상품이 없습니다.
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
        <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">

          <!-- 왼쪽 — 배송 정보 입력 -->
          <div class="space-y-4 lg:col-span-2">

            <!-- 주문 상품 목록 -->
            <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <h2 class="mb-4 text-base font-bold text-slate-800">주문 상품</h2>
              <ul class="divide-y divide-slate-100">
                <li
                  v-for="item in checkedItems"
                  :key="item.cartItemId"
                  class="flex items-start gap-4 py-3"
                >
                  <div class="flex-1 min-w-0">
                    <p class="font-medium text-slate-800 line-clamp-1">{{ item.title }}</p>
                    <p class="mt-0.5 text-sm text-slate-500">{{ item.author }}</p>
                  </div>
                  <div class="shrink-0 text-right text-sm">
                    <p class="text-slate-500">{{ item.quantity }}권</p>
                    <p class="mt-0.5 font-semibold text-slate-800">{{ item.itemTotal?.toLocaleString() }}원</p>
                  </div>
                </li>
              </ul>
            </section>

            <!-- 배송지 정보 -->
            <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <h2 class="mb-4 text-base font-bold text-slate-800">배송 정보</h2>
              <div class="space-y-3">

                <!-- 수령인 -->
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-600">수령인 <span class="text-rose-500">*</span></label>
                  <input
                    v-model="form.receiverName"
                    type="text"
                    placeholder="수령인 이름"
                    class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 outline-none transition focus:border-brand-800 focus:ring-2 focus:ring-brand-800/10"
                  />
                </div>

                <!-- 연락처 -->
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-600">연락처 <span class="text-rose-500">*</span></label>
                  <input
                    v-model="form.receiverPhone"
                    type="tel"
                    placeholder="010-0000-0000"
                    class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 outline-none transition focus:border-brand-800 focus:ring-2 focus:ring-brand-800/10"
                  />
                </div>

                <!-- 우편번호 -->
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-600">우편번호 <span class="text-rose-500">*</span></label>
                  <input
                    v-model="form.deliveryZipCode"
                    type="text"
                    placeholder="12345"
                    maxlength="5"
                    class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 outline-none transition focus:border-brand-800 focus:ring-2 focus:ring-brand-800/10"
                  />
                </div>

                <!-- 주소 -->
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-600">주소 <span class="text-rose-500">*</span></label>
                  <input
                    v-model="form.deliveryAddress"
                    type="text"
                    placeholder="도로명 주소"
                    class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 outline-none transition focus:border-brand-800 focus:ring-2 focus:ring-brand-800/10"
                  />
                </div>

                <!-- 상세 주소 -->
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-600">상세 주소</label>
                  <input
                    v-model="form.deliveryAddressDetail"
                    type="text"
                    placeholder="동/호수 등 상세 주소"
                    class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 outline-none transition focus:border-brand-800 focus:ring-2 focus:ring-brand-800/10"
                  />
                </div>

                <!-- 배송 메모 -->
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-600">배송 메모</label>
                  <input
                    v-model="form.deliveryMemo"
                    type="text"
                    placeholder="배송 시 요청사항 (선택)"
                    class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 outline-none transition focus:border-brand-800 focus:ring-2 focus:ring-brand-800/10"
                  />
                </div>

              </div>
            </section>

          </div>

          <!-- 오른쪽 — 결제 요약 -->
          <div class="lg:col-span-1">
            <div class="sticky top-24 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <h2 class="mb-4 text-base font-bold text-slate-800">결제 금액</h2>

              <dl class="space-y-2 text-sm">
                <div class="flex justify-between text-slate-500">
                  <dt>상품 금액</dt>
                  <dd>{{ cartStore.checkedTotal.toLocaleString() }}원</dd>
                </div>
                <div class="flex justify-between text-slate-500">
                  <dt>배송비</dt>
                  <dd>무료</dd>
                </div>
                <div class="my-2 border-t border-slate-100" />
                <div class="flex justify-between text-base font-bold text-brand-800">
                  <dt>최종 결제</dt>
                  <dd>{{ cartStore.checkedTotal.toLocaleString() }}원</dd>
                </div>
              </dl>

              <button
                class="mt-6 w-full rounded-xl bg-accent-500 py-3 font-bold text-white transition hover:bg-accent-600 disabled:cursor-not-allowed disabled:opacity-50"
                :disabled="submitting || !isFormValid"
                @click="submitOrder"
              >
                {{ submitting ? '처리 중...' : '결제하기' }}
              </button>

              <p v-if="!isFormValid" class="mt-2 text-center text-xs text-rose-400">
                필수 항목을 모두 입력해주세요
              </p>
            </div>
          </div>

        </div>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { useOrderStore } from '@/store/order'
import { orderApi } from '@/api/order'

const router     = useRouter()
const cartStore  = useCartStore()
const orderStore = useOrderStore()

// 장바구니에서 체크된 항목
const checkedItems = computed(() => cartStore.items.filter(i => i.isChecked))

const form = ref({
  receiverName:          '',
  receiverPhone:         '',
  deliveryZipCode:       '',
  deliveryAddress:       '',
  deliveryAddressDetail: '',
  deliveryMemo:          '',
})

const submitting = ref(false)

// 필수 입력값 검증
const isFormValid = computed(() =>
  form.value.receiverName.trim() &&
  form.value.receiverPhone.trim() &&
  form.value.deliveryZipCode.trim() &&
  form.value.deliveryAddress.trim()
)

// ── 주문 생성 + 결제 화면 이동 ────────────────────────────────────────────────
async function submitOrder() {
  if (!isFormValid.value || submitting.value) return

  submitting.value = true
  try {
    // POST /api/order → { orderId, finalPrice, itemName }
    const { data } = await orderApi.create({
      cartItemIds:           checkedItems.value.map(i => i.cartItemId),
      receiverName:          form.value.receiverName,
      receiverPhone:         form.value.receiverPhone,
      deliveryZipCode:       form.value.deliveryZipCode,
      deliveryAddress:       form.value.deliveryAddress,
      deliveryAddressDetail: form.value.deliveryAddressDetail,
      deliveryMemo:          form.value.deliveryMemo,
    })

    // 결제 정보 store에 저장 → PaymentView에서 사용
    orderStore.setOrder(data.orderId, data.finalPrice, data.itemName)

    // 결제 화면으로 이동
    router.push('/payment')
  } catch (e) {
    alert('주문 생성에 실패했습니다.')
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>
