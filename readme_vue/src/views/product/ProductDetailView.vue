<template>
  <div class="space-y-6">
    <section v-if="loading" class="page-section text-center">
      <p class="py-16 text-sm text-slate-500">상품 정보를 불러오는 중입니다.</p>
    </section>

    <section v-else-if="error" class="page-section text-center">
      <p class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-12 text-sm text-rose-700">{{ error }}</p>
    </section>

    <template v-else-if="product">
      <section class="page-section">
        <div class="grid gap-8 lg:grid-cols-[minmax(0,420px)_minmax(0,1fr)] lg:items-start">
          <div class="overflow-hidden rounded-[2rem] border border-slate-200 bg-slate-100">
            <img
              :src="resolveAssetUrl(product.thumbnail)"
              :alt="product.title"
              class="aspect-[3/4] w-full object-cover"
            />
          </div>

          <div class="space-y-6">
            <div class="space-y-3">
              <p class="text-sm font-semibold uppercase tracking-[0.18em] text-brand-700">Detail</p>
              <h1 class="text-3xl font-bold tracking-tight text-slate-900 lg:text-4xl">{{ product.title }}</h1>
              <p class="text-base text-slate-500">{{ product.author }}</p>
              <p class="text-sm leading-6 text-slate-500">도서 정보와 가격, 재고 상태를 확인하고 바로 장바구니에 담아보세요.</p>
            </div>

            <div class="surface-soft p-5">
              <div class="flex flex-wrap items-end gap-3">
                <span v-if="Number(product.discountRate) > 0" class="text-lg font-bold text-accent-600">
                  {{ Number(product.discountRate) }}%
                </span>
                <span class="text-3xl font-bold text-brand-800">{{ Number(product.salePrice).toLocaleString() }}원</span>
                <span class="text-base text-slate-400 line-through">{{ Number(product.price).toLocaleString() }}원</span>
              </div>
              <p class="mt-2 text-sm text-slate-500">예상 적립금 {{ Math.floor(product.salePrice * 0.1).toLocaleString() }}원</p>
            </div>

            <div class="grid gap-3 sm:grid-cols-2">
              <div class="surface-soft p-4">
                <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">재고</p>
                <p v-if="product.stock === 0" class="mt-2 text-sm font-semibold text-rose-600">현재 품절 상태입니다.</p>
                <p v-else-if="product.stock <= 5" class="mt-2 text-sm font-semibold text-amber-600">남은 재고 {{ product.stock }}권</p>
                <p v-else class="mt-2 text-sm font-semibold text-emerald-600">재고 여유 있음</p>
              </div>
              <div class="surface-soft p-4">
                <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">배송</p>
                <p class="mt-2 text-sm font-semibold text-slate-700">주문 확인 후 순차 출고</p>
                <p class="mt-1 text-xs leading-5 text-slate-500">배송 현황은 마이페이지에서 확인하실 수 있습니다.</p>
              </div>
            </div>

            <div class="flex flex-col gap-4 rounded-[1.75rem] border border-slate-200 bg-white p-5 shadow-sm sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p class="text-sm font-semibold text-slate-700">수량 선택</p>
                <div class="mt-3 inline-flex items-center overflow-hidden rounded-full border border-slate-200 bg-slate-50">
                  <button
                    type="button"
                    class="h-11 w-11 text-lg text-slate-600 transition hover:bg-slate-200 disabled:cursor-not-allowed disabled:text-slate-300"
                    :disabled="quantity <= 1"
                    @click="decreaseQty"
                  >
                    -
                  </button>
                  <span class="min-w-14 text-center text-base font-semibold text-slate-900">{{ quantity }}</span>
                  <button
                    type="button"
                    class="h-11 w-11 text-lg text-slate-600 transition hover:bg-slate-200 disabled:cursor-not-allowed disabled:text-slate-300"
                    :disabled="product.stock === 0 || quantity >= product.stock"
                    @click="increaseQty"
                  >
                    +
                  </button>
                </div>
              </div>

              <button
                type="button"
                class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                :disabled="product.stock === 0"
                @click="addToCart"
              >
                장바구니 담기
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="page-section">
        <div class="flex flex-wrap gap-2 border-b border-slate-200 pb-4">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="[
              'rounded-full px-4 py-2 text-sm font-semibold transition',
              activeTab === tab.key
                ? 'bg-brand-800 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
            ]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="pt-6">
          <div v-if="activeTab === 'info'" class="prose prose-slate max-w-none text-sm leading-7">
            <p>{{ product.description || '상품 설명이 아직 등록되지 않았습니다.' }}</p>
          </div>

          <div v-else-if="activeTab === 'review'" class="surface-soft px-6 py-12 text-center">
            <p class="text-base font-semibold text-slate-700">리뷰 서비스 준비 중입니다.</p>
            <p class="mt-2 text-sm text-slate-500">도서별 리뷰 기능은 추후 제공될 예정입니다.</p>
          </div>

          <div v-else-if="activeTab === 'qna'" class="surface-soft px-6 py-12 text-center">
            <p class="text-base font-semibold text-slate-700">Q&amp;A 서비스 준비 중입니다.</p>
            <p class="mt-2 text-sm text-slate-500">문의 등록과 답변 확인 기능은 추후 제공될 예정입니다.</p>
          </div>

          <div v-else class="grid gap-4 lg:grid-cols-2">
            <div class="surface-soft p-5">
              <p class="text-sm font-semibold text-slate-800">배송 안내</p>
              <p class="mt-3 text-sm leading-6 text-slate-500">
                결제 완료 후 순차 출고되며, 배송 준비와 배송 중 상태를 마이페이지에서 확인할 수 있습니다.
              </p>
            </div>
            <div class="surface-soft p-5">
              <p class="text-sm font-semibold text-slate-800">교환/반품 안내</p>
              <p class="mt-3 text-sm leading-6 text-slate-500">
                상품 상태와 배송 단계에 따라 교환 및 반품이 가능하며, 자세한 기준은 고객센터 안내를 따라 주세요.
              </p>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { productApi } from '@/api/product';
import { useCartStore } from '@/store/cart';
import { useAuthStore } from '@/store/auth';
import { resolveAssetUrl } from '@/utils/asset';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const product = ref(null);
const loading = ref(false);
const error = ref(null);
const quantity = ref(1);

const tabs = [
  { key: 'info', label: '상품 정보' },
  { key: 'review', label: '리뷰' },
  { key: 'qna', label: 'Q&A' },
  { key: 'delivery', label: '배송/교환' },
];
const activeTab = ref('info');

function increaseQty() {
  if (product.value && quantity.value < product.value.stock) {
    quantity.value += 1;
  }
}

function decreaseQty() {
  if (quantity.value > 1) {
    quantity.value -= 1;
  }
}

async function addToCart() {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.');
    router.push('/signin');
    return;
  }

  try {
    await cartStore.addItem(product.value.id, quantity.value);
    alert(`장바구니에 ${quantity.value}권을 담았습니다.`);
  } catch (e) {
    alert('장바구니 담기에 실패했습니다.');
  }
}

async function fetchProduct() {
  loading.value = true;
  error.value = null;
  quantity.value = 1;

  try {
    const { data } = await productApi.getDetail(route.params.productId);
    product.value = data;
  } catch (e) {
    error.value = e.response?.status === 404
      ? '존재하지 않는 상품입니다.'
      : '상품 정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
}

watch(
  () => route.params.productId,
  () => {
    fetchProduct();
  },
);

onMounted(() => {
  fetchProduct();
});
</script>
