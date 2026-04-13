<template>
  <div class="space-y-6">
    <section class="page-section">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-[0.18em] text-brand-700">Cart</p>
          <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">장바구니</h1>
          <p class="mt-2 text-sm leading-6 text-slate-500">선택한 도서를 확인하고 주문을 진행하실 수 있습니다.</p>
        </div>
        <p class="text-sm text-slate-400">총 {{ cartStore.items.length }}개 상품</p>
      </div>
    </section>

    <section v-if="cartStore.loading" class="page-section text-center">
      <p class="py-16 text-sm text-slate-500">장바구니를 불러오는 중입니다.</p>
    </section>

    <section v-else-if="cartStore.error" class="page-section text-center">
      <p class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-12 text-sm text-rose-700">{{ cartStore.error }}</p>
    </section>

    <section v-else-if="cartStore.items.length === 0" class="page-section text-center">
      <p class="text-base font-semibold text-slate-700">장바구니가 비어 있습니다.</p>
      <p class="mt-2 text-sm text-slate-500">도서를 담으면 이곳에서 주문을 진행하실 수 있습니다.</p>
      <button
        type="button"
        class="mt-6 rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
        @click="router.push('/product')"
      >
        도서 보러가기
      </button>
    </section>

    <section v-else class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
      <div class="page-section p-0 overflow-hidden">
        <div class="flex flex-col gap-4 border-b border-slate-200 px-5 py-4 sm:flex-row sm:items-center sm:justify-between lg:px-6">
          <label class="inline-flex items-center gap-3 text-sm font-semibold text-slate-700">
            <input type="checkbox" :checked="isAllChecked" @change="toggleAll" />
            전체 선택
          </label>
          <button
            type="button"
            class="rounded-full border border-slate-200 px-4 py-2 text-sm text-slate-600 transition hover:border-rose-200 hover:text-rose-600"
            @click="removeCheckedItems"
          >
            선택 상품 삭제
          </button>
        </div>

        <div class="divide-y divide-slate-200">
          <article
            v-for="item in cartStore.items"
            :key="item.cartItemId"
            class="grid gap-4 px-5 py-5 lg:grid-cols-[28px_minmax(0,1fr)_120px_150px_140px] lg:items-center lg:px-6"
          >
            <div class="pt-1">
              <input
                type="checkbox"
                :checked="item.isChecked"
                @change="cartStore.toggleCheck(item.cartItemId)"
              />
            </div>

            <div class="flex min-w-0 items-center gap-4">
              <img
                :src="resolveAssetUrl(item.thumbnail)"
                :alt="item.title"
                class="h-28 w-20 shrink-0 cursor-pointer rounded-2xl object-cover"
                @click="router.push(`/product/${item.productId}`)"
              />
              <div class="flex min-h-28 min-w-0 flex-1 flex-col justify-center">
                <button
                  type="button"
                  class="card-title-2 text-left text-base font-bold leading-6 text-slate-900 transition hover:text-brand-700"
                  @click="router.push(`/product/${item.productId}`)"
                >
                  {{ item.title }}
                </button>
                <p class="card-meta-1 mt-2 text-sm text-slate-500">{{ item.author }}</p>
                <p class="mt-3 text-xs font-semibold uppercase tracking-[0.16em] text-brand-700">빠른 배송 가능</p>
              </div>
            </div>

            <div class="numeric-stable text-sm font-semibold text-slate-700 lg:text-right">
              {{ Number(item.salePrice).toLocaleString() }}원
            </div>

            <div class="flex min-h-10 items-center lg:justify-self-center">
              <div class="inline-flex items-center overflow-hidden rounded-full border border-slate-200 bg-slate-50">
                <button
                  type="button"
                  class="h-10 w-10 text-lg text-slate-600 transition hover:bg-slate-200 disabled:cursor-not-allowed disabled:text-slate-300"
                  :disabled="item.quantity <= 1"
                  @click="handleUpdateQty(item, item.quantity - 1)"
                >
                  -
                </button>
                <span class="numeric-stable min-w-12 text-center text-sm font-semibold text-slate-900">{{ item.quantity }}</span>
                <button
                  type="button"
                  class="h-10 w-10 text-lg text-slate-600 transition hover:bg-slate-200"
                  @click="handleUpdateQty(item, item.quantity + 1)"
                >
                  +
                </button>
              </div>
            </div>

            <div class="numeric-stable flex min-h-10 items-center text-base font-bold text-brand-800 lg:justify-end lg:text-right">
              {{ Number(item.salePrice * item.quantity).toLocaleString() }}원
            </div>
          </article>
        </div>
      </div>

      <aside class="page-section xl:sticky xl:top-28">
        <h2 class="text-xl font-bold text-slate-900">주문 요약</h2>

        <div class="mt-6 space-y-4 text-sm">
          <div class="flex items-center justify-between text-slate-600">
            <span>총 상품 금액</span>
            <span class="font-semibold text-slate-800">{{ cartStore.checkedOriginalTotal.toLocaleString() }}원</span>
          </div>
          <div class="flex items-center justify-between text-slate-600">
            <span>총 할인 금액</span>
            <span class="font-semibold text-accent-600">-0원</span>
          </div>
          <div class="flex items-center justify-between text-slate-600">
            <span>배송비</span>
            <span class="font-semibold text-emerald-600">무료</span>
          </div>
        </div>

        <div class="my-6 h-px bg-slate-200"></div>

        <div class="flex items-end justify-between gap-4">
          <div>
            <p class="text-sm text-slate-500">결제 예정 금액</p>
            <p class="mt-2 text-3xl font-bold text-brand-800">{{ cartStore.checkedTotal.toLocaleString() }}원</p>
          </div>
          <span class="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-700">
            {{ checkedCount }}개 선택
          </span>
        </div>

        <button
          type="button"
          class="mt-6 w-full rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          :disabled="checkedCount === 0"
          @click="goToOrder"
        >
          주문하기
        </button>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/store/cart';
import { useOrderStore } from '@/store/order';
import { resolveAssetUrl } from '@/utils/asset';

const router = useRouter();
const cartStore = useCartStore();
const orderStore = useOrderStore();

const isAllChecked = computed(() =>
  cartStore.items.length > 0 && cartStore.items.every((item) => item.isChecked),
);

const checkedCount = computed(() =>
  cartStore.items.filter((item) => item.isChecked).length,
);

function toggleAll(event) {
  cartStore.toggleAll(event.target.checked);
}

async function handleUpdateQty(item, newQty) {
  if (newQty < 1) {
    if (confirm('이 상품을 장바구니에서 삭제할까요?')) {
      await cartStore.removeItem(item.cartItemId);
    }
    return;
  }

  try {
    await cartStore.updateItem(item.cartItemId, newQty);
  } catch (e) {
    alert('수량 변경에 실패했습니다.');
  }
}

async function removeCheckedItems() {
  const checkedItems = cartStore.items.filter((item) => item.isChecked);
  if (checkedItems.length === 0) {
    alert('삭제할 상품을 선택해 주세요.');
    return;
  }

  if (!confirm(`선택한 ${checkedItems.length}개 상품을 삭제할까요?`)) {
    return;
  }

  for (const item of checkedItems) {
    await cartStore.removeItem(item.cartItemId);
  }
}

function goToOrder() {
  if (checkedCount.value === 0) {
    alert('주문할 상품을 선택해 주세요.');
    return;
  }

  const checkedItems = cartStore.items.filter((item) => item.isChecked);
  const itemName = checkedItems.length === 1
    ? checkedItems[0].title
    : `${checkedItems[0].title} 외 ${checkedItems.length - 1}건`;

  orderStore.setOrder(null, cartStore.checkedTotal, itemName);
  const ids = checkedItems.map((item) => item.cartItemId).join(',');
  router.push({ path: '/order', query: { cartItemIds: ids } });
}

onMounted(() => {
  cartStore.fetchCart();
});
</script>
