<template>
  <section class="space-y-8">
    <section class="relative overflow-hidden rounded-[2rem] bg-brand-800 px-8 py-10 text-white shadow-2xl shadow-brand-900/20 md:px-10 md:py-12">
      <div class="absolute right-0 top-0 h-52 w-52 rounded-full bg-white/10 blur-3xl"></div>
      <div class="absolute bottom-0 left-16 h-28 w-28 rounded-full bg-accent-400/30 blur-2xl"></div>

      <div class="relative max-w-3xl space-y-5">
        <span class="point-chip !bg-white/10 !text-brand-100">ReadMe 큐레이션</span>
        <h1 class="text-4xl font-bold leading-tight tracking-tight text-white md:text-5xl">
          오늘 읽고 싶은 책을
          <br>
          가장 빠르게 만나는 공간
        </h1>
        <p class="max-w-2xl text-sm leading-7 text-brand-100 md:text-base">
          베스트셀러, 신간 도서, 추천 상품을 한눈에 살펴보고 카테고리별 탐색과 실제 리뷰까지 자연스럽게 이어지는
          도서 탐색 경험을 제공합니다.
        </p>
        <div class="flex flex-wrap gap-3 pt-2">
          <router-link
            class="rounded-full bg-white px-6 py-3 text-sm font-bold text-brand-800 transition hover:bg-brand-50"
            to="/product"
          >
            전체 도서 보러가기
          </router-link>
          <router-link
            class="rounded-full border border-white/25 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
            :to="authStore.isLoggedIn ? '/mypage' : '/signin'"
          >
            {{ authStore.isLoggedIn ? '마이페이지' : '로그인' }}
          </router-link>
        </div>
      </div>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-6 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Book Lineup</p>
          <h2 class="section-title mt-2">{{ activeSection.title }}</h2>
          <p class="mt-2 text-sm leading-6 text-slate-500">{{ activeSection.description }}</p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="section in featuredSections"
            :key="section.key"
            type="button"
            class="rounded-full border px-4 py-2 text-sm font-semibold transition"
            :class="activeSectionKey === section.key
              ? 'border-brand-700 bg-brand-800 text-white'
              : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200 hover:text-brand-800'"
            @click="activeSectionKey = section.key"
          >
            {{ section.label }}
          </button>
        </div>
      </div>

      <div v-if="featuredLoading" class="py-16 text-center text-sm text-slate-400">도서 목록을 불러오는 중입니다.</div>
      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <article
          v-for="product in activeProducts"
          :key="`${activeSectionKey}-${product.id}`"
          class="surface-soft flex h-full cursor-pointer flex-col overflow-hidden p-4 transition hover:-translate-y-0.5 hover:shadow-md"
          @click="router.push(`/product/${product.id}`)"
        >
          <img
            :src="resolveAssetUrl(product.thumbnail)"
            :alt="product.title"
            class="aspect-[3/4] w-full rounded-[1.25rem] bg-slate-100 object-cover"
          />
          <div class="mt-4 flex flex-1 flex-col">
            <div class="flex items-center justify-between gap-2">
              <span class="point-chip">{{ activeSection.badge }}</span>
              <span v-if="activeSectionKey === 'best'" class="text-xs font-semibold text-slate-400">판매 {{ product.salesCount ?? 0 }}</span>
              <span v-else-if="activeSectionKey === 'recommend'" class="text-xs font-semibold text-slate-400">재고 {{ product.stock ?? 0 }}</span>
              <span v-else class="text-xs font-semibold text-slate-400">{{ formatDate(product.createdAt) }}</span>
            </div>
            <h3 class="mt-3 line-clamp-2 text-base font-bold text-slate-900">{{ product.title }}</h3>
            <p class="mt-2 text-sm text-slate-500">{{ product.author }}</p>
            <div class="mt-auto pt-4">
              <p class="text-lg font-bold text-brand-800">{{ Number(product.salePrice).toLocaleString() }}원</p>
              <p class="mt-1 text-sm text-slate-400 line-through">{{ Number(product.price).toLocaleString() }}원</p>
            </div>
          </div>
        </article>
      </div>

      <div class="mt-6 flex justify-end">
        <button
          type="button"
          class="text-sm font-semibold text-brand-800 transition hover:text-accent-500"
          @click="goToFeaturedList(activeSection)"
        >
          더보기
        </button>
      </div>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Category</p>
          <h2 class="section-title mt-2">카테고리별 도서 둘러보기</h2>
        </div>
        <router-link class="text-sm font-semibold text-brand-800 transition hover:text-accent-500" to="/product">
          전체 카테고리 보기
        </router-link>
      </div>

      <div v-if="categoriesLoading" class="py-12 text-center text-sm text-slate-400">카테고리를 불러오는 중입니다.</div>
      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <article
          v-for="category in visibleCategories"
          :key="category.id"
          class="surface-soft p-5"
        >
          <button
            type="button"
            class="flex w-full items-center justify-between text-left"
            @click="goToCategory(category)"
          >
            <div>
              <p class="text-lg font-bold text-slate-900">{{ category.name }}</p>
              <p class="mt-2 text-sm text-slate-500">관련 도서를 카테고리별로 빠르게 확인해보세요.</p>
            </div>
            <span class="text-xl text-brand-700">›</span>
          </button>

          <div class="mt-4 flex flex-wrap gap-2">
            <button
              v-for="subCategory in category.subCategories.slice(0, 4)"
              :key="subCategory.id"
              type="button"
              class="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-brand-200 hover:text-brand-800"
              @click="goToSubCategory(category, subCategory)"
            >
              {{ subCategory.name }}
            </button>
          </div>
        </article>
      </div>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Recent Review</p>
          <h2 class="section-title mt-2">최근 리뷰</h2>
        </div>
        <router-link class="text-sm font-semibold text-brand-800 transition hover:text-accent-500" to="/review">
          더보기
        </router-link>
      </div>

      <div v-if="recentReviewsLoading" class="py-12 text-center text-sm text-slate-400">리뷰를 불러오는 중입니다.</div>
      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <article
          v-for="review in recentReviews"
          :key="review.reviewId"
          class="surface-soft flex h-full cursor-pointer flex-col p-4 transition hover:-translate-y-0.5 hover:shadow-md"
          @click="router.push(`/review/${review.reviewId}`)"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="truncate text-xs font-semibold uppercase tracking-[0.16em] text-brand-700">{{ review.productTitle }}</p>
              <h3 class="mt-2 line-clamp-2 text-base font-bold text-slate-900">{{ makeReviewTitle(review.content) }}</h3>
            </div>
            <span class="shrink-0 text-sm font-semibold text-amber-500">{{ '★'.repeat(review.rating) }}</span>
          </div>

          <p class="mt-3 text-sm font-semibold text-slate-600">{{ review.memberName }}</p>
          <p class="mt-3 line-clamp-3 text-sm leading-6 text-slate-500">{{ review.content }}</p>

          <div class="mt-auto pt-4 text-xs text-slate-400">
            {{ formatDate(review.createdAt) }}
          </div>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { productApi } from '@/api/product';
import { categoryApi } from '@/api/category';
import { reviewApi } from '@/api/board';
import { resolveAssetUrl } from '@/utils/asset';

const router = useRouter();
const authStore = useAuthStore();

const featuredSections = [
  {
    key: 'best',
    label: '베스트셀러',
    title: '많이 판매된 도서',
    description: '판매 수가 가장 많은 도서를 기준으로 지금 가장 주목받는 책을 보여드립니다.',
    badge: 'BEST',
    sortField: 'salesCount',
    sortDirection: 'desc',
  },
  {
    key: 'new',
    label: '신간 도서',
    title: '가장 최근에 등록된 도서',
    description: '최근 등록일 기준으로 가장 빠르게 업데이트된 신간 도서를 만나보세요.',
    badge: 'NEW',
    sortField: 'createdAt',
    sortDirection: 'desc',
  },
  {
    key: 'recommend',
    label: '추천 상품',
    title: '재고가 넉넉한 추천 상품',
    description: '재고 수량이 많은 도서를 중심으로 바로 구매하기 좋은 추천 상품을 준비했습니다.',
    badge: 'PICK',
    sortField: 'stock',
    sortDirection: 'desc',
  },
];

const activeSectionKey = ref('best');
const featuredProducts = ref({
  best: [],
  new: [],
  recommend: [],
});
const categories = ref([]);
const recentReviews = ref([]);
const featuredLoading = ref(true);
const categoriesLoading = ref(true);
const recentReviewsLoading = ref(true);

const activeSection = computed(
  () => featuredSections.find((section) => section.key === activeSectionKey.value) ?? featuredSections[0],
);

const activeProducts = computed(() => featuredProducts.value[activeSectionKey.value] ?? []);
const visibleCategories = computed(() => categories.value.slice(0, 8));

function formatDate(value) {
  if (!value) return '최근 등록';
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

function makeReviewTitle(content) {
  const text = (content || '').trim();
  if (!text) return '리뷰 상세 보기';
  return text.length > 30 ? `${text.slice(0, 30)}...` : text;
}

function goToFeaturedList(section) {
  router.push({
    path: '/product',
    query: {
      sortField: section.sortField,
      sortDirection: section.sortDirection,
    },
  });
}

function goToCategory(category) {
  router.push({
    path: '/product',
    query: {
      topId: category.id,
    },
  });
}

function goToSubCategory(category, subCategory) {
  router.push({
    path: '/product',
    query: {
      topId: category.id,
      subId: subCategory.id,
    },
  });
}

async function fetchFeaturedProducts() {
  featuredLoading.value = true;
  try {
    const responses = await Promise.all(
      featuredSections.map((section) =>
        productApi.getList({
          page: 0,
          size: 5,
          sort: `${section.sortField},${section.sortDirection}`,
        }),
      ),
    );

    const nextState = {};
    featuredSections.forEach((section, index) => {
      nextState[section.key] = responses[index].data.content ?? [];
    });
    featuredProducts.value = nextState;
  } catch (error) {
    console.error('홈 도서 목록 조회 실패', error);
    featuredProducts.value = { best: [], new: [], recommend: [] };
  } finally {
    featuredLoading.value = false;
  }
}

async function fetchCategories() {
  categoriesLoading.value = true;
  try {
    const { data } = await categoryApi.getTopCategories();
    categories.value = (data || []).filter((category) => category.status !== 'DELETE');
  } catch (error) {
    console.error('홈 카테고리 조회 실패', error);
    categories.value = [];
  } finally {
    categoriesLoading.value = false;
  }
}

async function fetchRecentReviews() {
  recentReviewsLoading.value = true;
  try {
    const { data } = await reviewApi.getRecent({ page: 0, size: 5 });
    recentReviews.value = data.content ?? [];
  } catch (error) {
    console.error('최근 리뷰 조회 실패', error);
    recentReviews.value = [];
  } finally {
    recentReviewsLoading.value = false;
  }
}

onMounted(() => {
  fetchFeaturedProducts();
  fetchCategories();
  fetchRecentReviews();
});
</script>
