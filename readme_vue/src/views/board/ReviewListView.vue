<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">상품 리뷰</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        상품별 리뷰를 모아 보고, 상세 리뷰 반응까지 확인할 수 있습니다.
      </p>
    </header>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Product Filter</p>
          <h2 class="mt-1 text-lg font-bold text-slate-900">리뷰를 볼 상품 선택</h2>
        </div>
        <span class="point-chip">{{ products.length }}권</span>
      </div>

      <div v-if="productsLoading" class="py-10 text-center text-sm text-slate-400">상품 목록을 불러오는 중입니다.</div>

      <div v-else class="flex flex-wrap gap-3">
        <button
          v-for="product in products"
          :key="product.productId"
          class="rounded-full border px-4 py-2 text-sm font-semibold transition"
          :class="selectedProductId === product.productId
            ? 'border-brand-800 bg-brand-800 text-white'
            : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200 hover:text-brand-800'"
          @click="changeProduct(product.productId)"
        >
          {{ product.title }}
        </button>
      </div>
    </section>

    <section class="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
      <div class="surface-panel p-6">
        <div class="mb-5 flex items-center justify-between">
          <div>
            <p class="text-sm font-semibold text-brand-700">Review List</p>
            <h2 class="mt-1 text-lg font-bold text-slate-900">
              {{ currentProductTitle }}
            </h2>
          </div>
          <span class="point-chip">{{ reviews.length }}건</span>
        </div>

        <div v-if="reviewsLoading" class="py-14 text-center text-sm text-slate-400">리뷰를 불러오는 중입니다.</div>

        <div v-else-if="reviews.length === 0" class="surface-soft py-14 text-center text-sm text-slate-400">
          선택한 상품에 등록된 리뷰가 없습니다.
        </div>

        <div v-else class="space-y-3">
          <button
            v-for="review in reviews"
            :key="review.reviewId"
            class="w-full rounded-[1.5rem] border px-5 py-4 text-left transition"
            :class="selectedReview?.reviewId === review.reviewId
              ? 'border-brand-200 bg-brand-50/60 shadow-sm'
              : 'border-slate-200 bg-white hover:border-brand-100 hover:bg-slate-50'"
            @click="selectReview(review.reviewId)"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="flex items-center gap-2">
                  <span class="point-chip">{{ renderStars(review.rating) }}</span>
                  <p class="font-semibold text-slate-900">{{ review.memberName }}</p>
                </div>
                <p class="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">{{ review.content }}</p>
                <p class="mt-2 text-xs text-slate-400">{{ formatDate(review.createdAt) }}</p>
              </div>
              <span class="text-xs text-slate-400">조회 {{ review.hits }}</span>
            </div>
          </button>
        </div>
      </div>

      <aside class="surface-panel p-6">
        <div class="mb-5 flex items-center justify-between">
          <div>
            <p class="text-sm font-semibold text-brand-700">Review Detail</p>
            <h2 class="mt-1 text-lg font-bold text-slate-900">상세 내용</h2>
          </div>
          <span v-if="selectedReview" class="point-chip">선택됨</span>
        </div>

        <div v-if="detailLoading" class="py-14 text-center text-sm text-slate-400">상세 리뷰를 불러오는 중입니다.</div>

        <div v-else-if="!selectedReview" class="surface-soft py-14 text-center text-sm text-slate-400">
          왼쪽 목록에서 리뷰를 선택해주세요.
        </div>

        <div v-else class="space-y-5">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-700">상품명</p>
            <p class="mt-2 text-lg font-bold text-slate-900">{{ selectedReview.productTitle }}</p>
          </div>

          <div class="surface-soft p-5">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="font-semibold text-slate-900">{{ selectedReview.memberName }}</p>
                <p class="mt-1 text-sm text-slate-500">{{ formatDate(selectedReview.createdAt) }}</p>
              </div>
              <span class="point-chip">{{ renderStars(selectedReview.rating) }}</span>
            </div>
            <p class="mt-4 text-sm leading-7 text-slate-700">{{ selectedReview.content }}</p>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <button
              class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-100"
              @click="react('LIKE')"
            >
              좋아요 {{ selectedReview.likeCount ?? 0 }}
            </button>
            <button
              class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-semibold text-rose-700 transition hover:bg-rose-100"
              @click="react('DISLIKE')"
            >
              별로예요 {{ selectedReview.dislikeCount ?? 0 }}
            </button>
          </div>
        </div>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { productApi } from '@/api/product';
import { reviewApi } from '@/api/board';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const products = ref([]);
const selectedProductId = ref(null);
const reviews = ref([]);
const selectedReview = ref(null);
const productsLoading = ref(true);
const reviewsLoading = ref(true);
const detailLoading = ref(false);

const currentProductTitle = computed(() => {
  return products.value.find((product) => product.productId === selectedProductId.value)?.title || '선택한 상품';
});

async function fetchProducts() {
  productsLoading.value = true;
  try {
    const { data } = await productApi.getList({ page: 0, size: 10 });
    products.value = data.content ?? [];
    if (products.value[0]) {
      selectedProductId.value = products.value[0].productId;
      await fetchReviews();
    }
  } catch (error) {
    console.error('상품 목록 조회 실패', error);
    products.value = [];
  } finally {
    productsLoading.value = false;
  }
}

async function fetchReviews() {
  if (!selectedProductId.value) return;

  reviewsLoading.value = true;
  selectedReview.value = null;
  try {
    const { data } = await reviewApi.getList({
      productId: selectedProductId.value,
      page: 0,
      size: 20,
      sort: 'createdAt,desc',
    });
    reviews.value = data.content ?? [];
    if (reviews.value[0]) {
      await selectReview(reviews.value[0].reviewId);
    }
  } catch (error) {
    console.error('리뷰 목록 조회 실패', error);
    reviews.value = [];
  } finally {
    reviewsLoading.value = false;
  }
}

async function selectReview(reviewId) {
  detailLoading.value = true;
  try {
    const { data } = await reviewApi.getDetail(reviewId);
    selectedReview.value = data;
  } catch (error) {
    console.error('리뷰 상세 조회 실패', error);
  } finally {
    detailLoading.value = false;
  }
}

async function changeProduct(productId) {
  selectedProductId.value = productId;
  await fetchReviews();
}

async function react(reactionType) {
  if (!authStore.isLoggedIn || !selectedReview.value) {
    alert('로그인 후 리뷰 반응을 남길 수 있습니다.');
    return;
  }

  try {
    await reviewApi.react(selectedReview.value.reviewId, reactionType);
    await selectReview(selectedReview.value.reviewId);
  } catch (error) {
    console.error('리뷰 반응 처리 실패', error);
    alert('리뷰 반응 처리에 실패했습니다.');
  }
}

function renderStars(rating) {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating);
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

onMounted(fetchProducts);
</script>
