<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <div v-if="loading" class="py-20 text-center text-sm text-slate-400">리뷰 상세를 불러오는 중입니다.</div>

      <template v-else-if="review">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p class="text-sm font-semibold text-brand-700">Review Detail</p>
            <h1 class="section-title mt-3">{{ reviewTitle }}</h1>
            <p class="mt-2 text-sm text-slate-500">{{ review.productTitle }}에 대한 독자 리뷰를 자세히 확인해 보세요.</p>
          </div>
          <button
            type="button"
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="router.push('/review')"
          >
            목록으로
          </button>
        </div>

        <dl class="mt-6 grid gap-3 sm:grid-cols-4">
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">{{ review.memberName }}</dd>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">평점</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">{{ review.rating }} / 5</dd>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성일</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">{{ formatDate(review.createdAt) }}</dd>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">반응</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">좋아요 {{ review.likeCount }} / 별로예요 {{ review.dislikeCount }}</dd>
          </div>
        </dl>

        <div class="mt-6 rounded-[1.75rem] bg-slate-50 px-6 py-7 text-sm leading-7 text-slate-700 whitespace-pre-line">
          {{ review.content }}
        </div>

        <div v-if="review.imageUrls?.length" class="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <img
            v-for="(imageUrl, index) in review.imageUrls"
            :key="`${imageUrl}-${index}`"
            :src="imageUrl"
            :alt="`리뷰 이미지 ${index + 1}`"
            class="h-60 w-full rounded-[1.25rem] border border-slate-200 object-cover"
          />
        </div>

        <div class="mt-6">
          <router-link
            :to="`/product/${review.productId}`"
            class="inline-flex rounded-full bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
          >
            도서 상세 보기
          </router-link>
        </div>
      </template>

      <div v-else class="py-20 text-center text-sm text-slate-400">리뷰를 찾을 수 없습니다.</div>
    </header>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { reviewApi } from '@/api/board';

const route = useRoute();
const router = useRouter();
const review = ref(null);
const loading = ref(true);

const reviewTitle = computed(() => {
  const text = review.value?.content?.trim() || '';
  return text.length > 28 ? `${text.slice(0, 28)}...` : text || '리뷰 상세';
});

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

async function fetchReviewDetail() {
  loading.value = true;
  try {
    const { data } = await reviewApi.getDetail(route.params.reviewId);
    review.value = data;
  } catch (error) {
    console.error('리뷰 상세 조회 실패', error);
    review.value = null;
  } finally {
    loading.value = false;
  }
}

onMounted(fetchReviewDetail);
</script>
