<template>
  <div class="space-y-6">
    <section class="page-section">
      <div class="flex flex-col gap-5">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="text-sm font-semibold uppercase tracking-[0.18em] text-brand-700">Catalog</p>
            <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">도서 목록</h1>
            <p class="mt-2 text-sm leading-6 text-slate-500">
              카테고리와 검색 조건으로 도서를 찾으실 수 있습니다.
            </p>
          </div>

          <div class="flex w-full flex-col gap-3 lg:max-w-xl">
            <div class="flex overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <select
                v-model="searchType"
                class="w-28 border-r border-slate-200 bg-slate-50 px-3 text-sm text-slate-600 outline-none"
              >
                <option value="title">도서명</option>
                <option value="author">저자</option>
              </select>
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="검색어를 입력해 주세요"
                class="min-w-0 flex-1 px-4 py-3 text-sm text-slate-700 outline-none"
                @keyup.enter="handleSearch"
              />
              <button
                class="bg-brand-800 px-5 text-sm font-semibold text-white transition hover:bg-brand-700"
                @click="handleSearch"
              >
                검색
              </button>
            </div>
          </div>
        </div>

        <div class="flex flex-wrap gap-2">
          <button
            v-for="tab in categoryTabs"
            :key="String(tab.topId)"
            type="button"
            :class="[
              'rounded-full border px-4 py-2 text-sm font-semibold transition',
              selectedTopId === tab.topId
                ? 'border-brand-700 bg-brand-800 text-white'
                : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200 hover:text-brand-800',
            ]"
            @click="selectCategory(tab.topId)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="subCategoryTabs.length" class="flex flex-wrap gap-2">
          <button
            v-for="sub in subCategoryTabs"
            :key="String(sub.subId)"
            type="button"
            :class="[
              'rounded-full border px-3 py-1.5 text-xs font-semibold transition',
              selectedSubId === sub.subId
                ? 'border-accent-400 bg-accent-500 text-white'
                : 'border-slate-200 bg-slate-50 text-slate-600 hover:border-accent-200 hover:text-accent-600',
            ]"
            @click="selectSubCategory(sub.subId)"
          >
            {{ sub.label }}
          </button>
        </div>
      </div>
    </section>

    <section v-if="productStore.loading" class="page-section text-center">
      <p class="py-16 text-sm text-slate-500">도서 목록을 불러오는 중입니다.</p>
    </section>

    <section v-else-if="productStore.error" class="page-section text-center">
      <p class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-12 text-sm text-rose-700">
        {{ productStore.error }}
      </p>
    </section>

    <template v-else>
      <section class="page-section">
        <div class="mb-5 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p class="text-sm font-semibold text-brand-700">Results</p>
            <p class="text-sm text-slate-500">총 {{ productStore.totalElements.toLocaleString() }}권의 도서가 검색되었습니다.</p>
          </div>
          <p class="text-xs text-slate-400">최신 등록순 기준</p>
        </div>

        <div v-if="productStore.products.length === 0" class="rounded-[1.5rem] border border-dashed border-slate-200 bg-slate-50 px-6 py-16 text-center">
          <p class="text-base font-semibold text-slate-700">검색 결과가 없습니다.</p>
          <p class="mt-2 text-sm text-slate-500">검색어 또는 카테고리를 다시 선택해 주세요.</p>
        </div>

        <div v-else class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
          <article
            v-for="product in productStore.products"
            :key="product.id"
            class="card-fixed group overflow-hidden rounded-[1.75rem] border border-slate-200 bg-white transition hover:-translate-y-1 hover:shadow-lg"
          >
            <div class="relative cursor-pointer overflow-hidden bg-slate-100" @click="goToDetail(product.id)">
              <img
                :src="resolveAssetUrl(product.thumbnail)"
                :alt="product.title"
                class="aspect-[3/4] w-full object-cover transition duration-300 group-hover:scale-[1.03]"
              />
              <div class="absolute left-4 top-4 flex gap-2">
                <span
                  v-if="product.salesCount > 1000"
                  class="rounded-full bg-brand-800 px-3 py-1 text-[11px] font-bold text-white"
                >
                  베스트
                </span>
                <span
                  v-else-if="Number(product.discountRate) >= 20"
                  class="rounded-full bg-accent-500 px-3 py-1 text-[11px] font-bold text-white"
                >
                  할인
                </span>
              </div>
            </div>

            <div class="flex flex-1 flex-col p-5">
              <button
                type="button"
                class="card-title-2 text-left text-base font-bold leading-6 text-slate-900 transition hover:text-brand-700"
                @click="goToDetail(product.id)"
              >
                {{ product.title }}
              </button>
              <p class="card-meta-1 mt-2 text-sm text-slate-500">{{ product.author }}</p>

              <div class="mt-auto flex items-end gap-2 pt-4">
                <span v-if="Number(product.discountRate) > 0" class="text-sm font-bold text-accent-600">
                  {{ Number(product.discountRate) }}%
                </span>
                <span class="numeric-stable text-xl font-bold text-brand-800">{{ Number(product.salePrice).toLocaleString() }}원</span>
              </div>
              <p class="numeric-stable mt-1 text-sm text-slate-400 line-through">{{ Number(product.price).toLocaleString() }}원</p>

              <button
                type="button"
                class="mt-5 rounded-2xl bg-brand-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
                @click="addToCart(product.id)"
              >
                장바구니 담기
              </button>
            </div>
          </article>
        </div>
      </section>

      <section v-if="productStore.totalPages > 1" class="flex justify-center">
        <div class="flex flex-wrap items-center justify-center gap-2 rounded-full border border-slate-200 bg-white px-3 py-2 shadow-sm">
          <button
            type="button"
            class="rounded-full px-3 py-2 text-sm text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="productStore.currentPage === 0"
            @click="changePage(0)"
          >
            처음
          </button>
          <button
            type="button"
            class="rounded-full px-3 py-2 text-sm text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="productStore.currentPage === 0"
            @click="changePage(productStore.currentPage - 1)"
          >
            이전
          </button>

          <button
            v-for="p in pageRange"
            :key="p"
            type="button"
            :class="[
              'h-10 min-w-10 rounded-full px-3 text-sm font-semibold transition',
              p === productStore.currentPage
                ? 'bg-brand-800 text-white'
                : 'text-slate-600 hover:bg-slate-100',
            ]"
            @click="changePage(p)"
          >
            {{ p + 1 }}
          </button>

          <button
            type="button"
            class="rounded-full px-3 py-2 text-sm text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="productStore.currentPage >= productStore.totalPages - 1"
            @click="changePage(productStore.currentPage + 1)"
          >
            다음
          </button>
          <button
            type="button"
            class="rounded-full px-3 py-2 text-sm text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="productStore.currentPage >= productStore.totalPages - 1"
            @click="changePage(productStore.totalPages - 1)"
          >
            끝
          </button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useCartStore } from '@/store/cart';
import { useProductStore } from '@/store/product';
import { resolveAssetUrl } from '@/utils/asset';

const router = useRouter();
const route = useRoute();
const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();

const selectedTopId = ref(null);
const selectedSubId = ref(null);
const searchKeyword = ref('');
const searchType = ref('title');

const categoryTabs = [
  { label: '전체', topId: null },
  { label: '국내도서', topId: 1 },
  { label: '해외도서', topId: 2 },
  { label: '일본도서', topId: 3 },
];

const subCategoryMap = {
  1: [
    { label: '전체', subId: null },
    { label: '소설', subId: 1 },
    { label: '자기계발', subId: 2 },
    { label: '경제/경영', subId: 3 },
    { label: '과학/기술', subId: 4 },
    { label: '역사/문화', subId: 5 },
  ],
  2: [
    { label: '전체', subId: null },
    { label: '소설', subId: 6 },
    { label: '자기계발', subId: 7 },
    { label: '비즈니스', subId: 8 },
    { label: '과학', subId: 9 },
    { label: '역사', subId: 10 },
  ],
  3: [
    { label: '전체', subId: null },
    { label: '소설', subId: 11 },
    { label: '자기계발', subId: 12 },
    { label: '비즈니스', subId: 13 },
    { label: '과학', subId: 14 },
    { label: '역사', subId: 15 },
  ],
};

const subCategoryTabs = computed(() => {
  if (selectedTopId.value === null) return [];
  return subCategoryMap[selectedTopId.value] ?? [];
});

const pageRange = computed(() => {
  const current = productStore.currentPage;
  const total = productStore.totalPages;
  const start = Math.max(0, current - 2);
  const end = Math.min(total - 1, current + 2);
  return Array.from({ length: end - start + 1 }, (_, index) => start + index);
});

function syncFromRoute() {
  selectedTopId.value = route.query.topId ? Number(route.query.topId) : null;
  selectedSubId.value = route.query.subId ? Number(route.query.subId) : null;
  searchKeyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : '';
  searchType.value = typeof route.query.searchType === 'string' ? route.query.searchType : 'title';
}

function updateRoute(page = 0) {
  const query = {};
  if (selectedTopId.value !== null) query.topId = selectedTopId.value;
  if (selectedSubId.value !== null) query.subId = selectedSubId.value;
  if (searchKeyword.value.trim()) query.keyword = searchKeyword.value.trim();
  if (searchType.value) query.searchType = searchType.value;
  if (page > 0) query.page = page;

  router.push({ path: '/product', query });
}

function selectCategory(topId) {
  selectedTopId.value = topId;
  selectedSubId.value = null;
  updateRoute(0);
}

function selectSubCategory(subId) {
  selectedSubId.value = subId;
  updateRoute(0);
}

function handleSearch() {
  updateRoute(0);
}

function changePage(page) {
  if (page < 0 || page >= productStore.totalPages) return;
  updateRoute(page);
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function goToDetail(productId) {
  router.push(`/product/${productId}`);
}

async function addToCart(productId) {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.');
    router.push('/signin');
    return;
  }

  try {
    await cartStore.addItem(productId, 1);
    alert('장바구니에 담았습니다.');
  } catch (e) {
    alert('장바구니 담기에 실패했습니다.');
  }
}

async function fetchByRoute() {
  syncFromRoute();
  const page = route.query.page ? Number(route.query.page) : 0;
  await productStore.fetchProducts(page, selectedTopId.value, selectedSubId.value, searchKeyword.value);
}

watch(
  () => route.fullPath,
  () => {
    fetchByRoute();
  },
);

onMounted(() => {
  fetchByRoute();
});
</script>
