<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">상품 리뷰</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        상품별 리뷰를 확인하고, 로그인한 사용자는 직접 리뷰를 작성하거나 수정할 수 있습니다.
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
          :key="product.id"
          class="rounded-full border px-4 py-2 text-sm font-semibold transition"
          :class="selectedProductId === product.id
            ? 'border-brand-800 bg-brand-800 text-white'
            : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200 hover:text-brand-800'"
          @click="changeProduct(product.id)"
        >
          {{ product.title }}
        </button>
      </div>
    </section>

    <section class="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
      <div class="space-y-6">
        <section class="surface-panel p-6">
          <div class="mb-5 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-brand-700">Write Review</p>
              <h2 class="mt-1 text-lg font-bold text-slate-900">{{ currentProductTitle }}</h2>
            </div>
            <span class="point-chip">{{ authStore.isLoggedIn ? '작성 가능' : '로그인 필요' }}</span>
          </div>

          <form class="space-y-4" @submit.prevent="submitReview">
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">평점</span>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="score in [1, 2, 3, 4, 5]"
                  :key="score"
                  type="button"
                  class="rounded-full border px-4 py-2 text-sm font-semibold transition"
                  :class="reviewForm.rating === score
                    ? 'border-brand-800 bg-brand-800 text-white'
                    : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200 hover:text-brand-800'"
                  @click="reviewForm.rating = score"
                >
                  {{ score }}점
                </button>
              </div>
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">리뷰 내용</span>
              <textarea
                v-model="reviewForm.content"
                class="surface-soft min-h-32 w-full px-4 py-3 text-sm outline-none"
                placeholder="상품을 사용해 본 경험을 적어 주세요"
              ></textarea>
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">리뷰 이미지 URL</span>
              <textarea
                v-model="reviewForm.imageUrlText"
                class="surface-soft min-h-24 w-full px-4 py-3 text-sm outline-none"
                placeholder="이미지 URL이 여러 개라면 줄바꿈으로 구분해 입력해 주세요"
              ></textarea>
            </label>

            <p v-if="formMessage" class="rounded-2xl px-4 py-3 text-sm" :class="formMessageClass">{{ formMessage }}</p>

            <div class="flex justify-end gap-3">
              <button
                v-if="editingReviewId"
                type="button"
                class="rounded-full border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                @click="cancelReviewEdit"
              >
                수정 취소
              </button>
              <button
                type="submit"
                class="rounded-full bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                :disabled="!authStore.isLoggedIn || reviewSubmitting || !selectedProductId"
              >
                {{ reviewSubmitting ? (editingReviewId ? '수정 중...' : '등록 중...') : (editingReviewId ? '리뷰 수정' : '리뷰 등록') }}
              </button>
            </div>
          </form>
        </section>

        <section class="surface-panel p-6">
          <div class="mb-5 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-brand-700">Review List</p>
              <h2 class="mt-1 text-lg font-bold text-slate-900">{{ currentProductTitle }}</h2>
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
                <div class="min-w-0">
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
        </section>
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
          왼쪽 목록에서 리뷰를 선택해 주세요.
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
            <p class="mt-4 whitespace-pre-line text-sm leading-7 text-slate-700">{{ selectedReview.content }}</p>
          </div>

          <div v-if="selectedReview.imageUrls?.length" class="grid grid-cols-2 gap-3">
            <img
              v-for="(imageUrl, index) in selectedReview.imageUrls"
              :key="`${selectedReview.reviewId}-${index}`"
              :src="imageUrl"
              :alt="`리뷰 이미지 ${index + 1}`"
              class="aspect-square w-full rounded-2xl border border-slate-200 object-cover"
            >
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

          <div v-if="canManageSelectedReview" class="flex gap-3">
            <button type="button" class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50" @click="startEditReview">
              리뷰 수정
            </button>
            <button type="button" class="rounded-2xl bg-rose-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-rose-600" @click="deleteSelectedReview">
              리뷰 삭제
            </button>
          </div>
        </div>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { productApi } from '@/api/product';
import { reviewApi } from '@/api/board';
import { memberApi } from '@/api/member';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const products = ref([]);
const selectedProductId = ref(null);
const reviews = ref([]);
const selectedReview = ref(null);
const currentMemberId = ref(null);
const productsLoading = ref(true);
const reviewsLoading = ref(true);
const detailLoading = ref(false);
const reviewSubmitting = ref(false);
const editingReviewId = ref(null);
const formMessage = ref('');
const formMessageClass = ref('bg-emerald-50 text-emerald-700');

const reviewForm = reactive({
  rating: 5,
  content: '',
  imageUrlText: '',
});

const currentProductTitle = computed(() => {
  return products.value.find((product) => product.id === selectedProductId.value)?.title || '선택한 상품';
});

const canManageSelectedReview = computed(() => {
  if (!selectedReview.value || !currentMemberId.value) return false;
  return selectedReview.value.memberId === currentMemberId.value;
});

async function initialize() {
  if (authStore.isLoggedIn) {
    try {
      const { data } = await memberApi.getMe();
      currentMemberId.value = data.id;
    } catch (error) {
      console.error('회원 정보 조회 실패', error);
    }
  }
  await fetchProducts();
}

async function fetchProducts() {
  productsLoading.value = true;
  try {
    const { data } = await productApi.getList({ page: 0, size: 10 });
    products.value = data.content ?? [];
    if (products.value[0]) {
      selectedProductId.value = products.value[0].id;
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
  cancelReviewEdit();
  await fetchReviews();
}

function parseImageUrls() {
  return reviewForm.imageUrlText
    .split('\n')
    .map((value) => value.trim())
    .filter(Boolean);
}

async function submitReview() {
  if (!authStore.isLoggedIn) {
    formMessage.value = '로그인 후 리뷰를 등록할 수 있습니다.';
    formMessageClass.value = 'bg-amber-50 text-amber-700';
    return;
  }
  if (!selectedProductId.value || !reviewForm.content.trim()) {
    formMessage.value = '상품을 선택하고 리뷰 내용을 입력해 주세요.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
    return;
  }

  reviewSubmitting.value = true;
  try {
    const payload = {
      productId: selectedProductId.value,
      rating: reviewForm.rating,
      content: reviewForm.content.trim(),
      imageUrls: parseImageUrls(),
    };

    if (editingReviewId.value) {
      await reviewApi.updateMy(editingReviewId.value, {
        rating: payload.rating,
        content: payload.content,
        imageUrls: payload.imageUrls,
      });
      formMessage.value = '리뷰가 수정되었습니다.';
    } else {
      await reviewApi.create(payload);
      formMessage.value = '리뷰가 등록되었습니다.';
    }

    formMessageClass.value = 'bg-emerald-50 text-emerald-700';
    cancelReviewEdit();
    await fetchReviews();
  } catch (error) {
    console.error('리뷰 저장 실패', error);
    formMessage.value = error.response?.data?.message || '리뷰 저장에 실패했습니다.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
  } finally {
    reviewSubmitting.value = false;
  }
}

function startEditReview() {
  if (!selectedReview.value) return;
  editingReviewId.value = selectedReview.value.reviewId;
  reviewForm.rating = selectedReview.value.rating;
  reviewForm.content = selectedReview.value.content;
  reviewForm.imageUrlText = (selectedReview.value.imageUrls || []).join('\n');
  formMessage.value = '리뷰 수정 모드입니다. 내용을 변경한 뒤 저장해 주세요.';
  formMessageClass.value = 'bg-sky-50 text-sky-700';
}

function cancelReviewEdit() {
  editingReviewId.value = null;
  reviewForm.rating = 5;
  reviewForm.content = '';
  reviewForm.imageUrlText = '';
}

async function deleteSelectedReview() {
  if (!selectedReview.value || !confirm('선택한 리뷰를 삭제하시겠습니까?')) return;
  try {
    await reviewApi.removeMy(selectedReview.value.reviewId);
    formMessage.value = '리뷰가 삭제되었습니다.';
    formMessageClass.value = 'bg-emerald-50 text-emerald-700';
    selectedReview.value = null;
    cancelReviewEdit();
    await fetchReviews();
  } catch (error) {
    formMessage.value = error.response?.data?.message || '리뷰 삭제에 실패했습니다.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
  }
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

onMounted(initialize);
</script>
