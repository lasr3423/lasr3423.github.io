<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">리뷰 관리</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        회원이 남긴 리뷰를 확인하고, 문제가 있는 리뷰는 상세 내용을 검토한 뒤 삭제할 수 있습니다.
      </p>
    </section>

    <section class="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_380px]">
      <div class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">리뷰 목록</h2>
            <p class="mt-1 text-sm text-slate-500">평점, 상품, 작성자 정보를 한 번에 확인할 수 있어요.</p>
          </div>
          <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
            총 {{ totalElements.toLocaleString() }}건
          </span>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-20 text-sm text-slate-400">
          리뷰 목록을 불러오는 중입니다.
        </div>

        <template v-else-if="reviews.length > 0">
          <div class="overflow-x-auto">
            <table class="w-full min-w-[860px] text-sm">
              <thead class="border-b border-slate-200 bg-slate-50">
                <tr>
                  <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">리뷰</th>
                  <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">상품</th>
                  <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</th>
                  <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">평점</th>
                  <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">반응</th>
                  <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr
                  v-for="review in reviews"
                  :key="review.reviewId"
                  class="cursor-pointer transition hover:bg-slate-50"
                  @click="selectReview(review.reviewId)"
                >
                  <td class="px-6 py-4">
                    <p class="font-semibold text-slate-900">리뷰 #{{ review.reviewId }}</p>
                    <p class="mt-1 line-clamp-2 max-w-md text-sm text-slate-600">{{ review.content }}</p>
                    <p class="mt-2 text-xs text-slate-400">{{ formatDateTime(review.createdAt) }}</p>
                  </td>
                  <td class="px-6 py-4">
                    <p class="font-medium text-slate-800">{{ review.productTitle || `상품 #${review.productId}` }}</p>
                    <p class="mt-1 text-xs text-slate-400">상품 ID {{ review.productId }}</p>
                  </td>
                  <td class="px-6 py-4">
                    <p class="font-medium text-slate-800">{{ review.memberName }}</p>
                    <p class="mt-1 text-xs text-slate-400">회원 ID {{ review.memberId }}</p>
                  </td>
                  <td class="px-6 py-4 text-center">
                    <span class="text-amber-400">{{ renderStars(review.rating) }}</span>
                    <p class="mt-1 text-xs font-medium text-slate-500">{{ review.rating }} / 5</p>
                  </td>
                  <td class="px-6 py-4 text-center">
                    <p class="font-semibold text-slate-700">좋아요 {{ review.likeCount }}</p>
                    <p class="mt-1 text-xs text-slate-400">싫어요 {{ review.dislikeCount }}</p>
                  </td>
                  <td class="px-6 py-4 text-right">
                    <div class="flex justify-end gap-2">
                      <button
                        type="button"
                        class="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
                        @click.stop="selectReview(review.reviewId)"
                      >
                        상세
                      </button>
                      <button
                        type="button"
                        class="rounded-xl bg-rose-500 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-rose-600"
                        @click.stop="handleDelete(review.reviewId)"
                      >
                        삭제
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
            <button type="button" :disabled="page === 0" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="changePage(page - 1)">
              이전
            </button>
            <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
            <button type="button" :disabled="page >= totalPages - 1" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="changePage(page + 1)">
              다음
            </button>
          </div>
        </template>

        <div v-else class="flex items-center justify-center py-20 text-sm text-slate-400">
          등록된 리뷰가 없습니다.
        </div>
      </div>

      <aside class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
        <template v-if="detailLoading">
          <div class="flex min-h-[320px] items-center justify-center text-sm text-slate-400">
            리뷰 상세를 불러오는 중입니다.
          </div>
        </template>

        <template v-else-if="selectedReview">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Review Detail</p>
              <h2 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">리뷰 #{{ selectedReview.reviewId }}</h2>
            </div>
            <button type="button" class="rounded-xl bg-rose-500 px-3 py-2 text-xs font-semibold text-white transition hover:bg-rose-600" @click="handleDelete(selectedReview.reviewId)">
              리뷰 삭제
            </button>
          </div>

          <dl class="mt-6 grid gap-4 sm:grid-cols-2">
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">상품</dt>
              <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedReview.productTitle || '-' }}</dd>
              <dd class="mt-1 text-xs text-slate-500">상품 ID {{ selectedReview.productId }}</dd>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</dt>
              <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedReview.memberName }}</dd>
              <dd class="mt-1 text-xs text-slate-500">회원 ID {{ selectedReview.memberId }}</dd>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">평점</dt>
              <dd class="mt-2 text-amber-400">{{ renderStars(selectedReview.rating) }}</dd>
              <dd class="mt-1 text-xs text-slate-500">{{ selectedReview.rating }} / 5</dd>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">반응</dt>
              <dd class="mt-2 text-sm font-semibold text-slate-900">좋아요 {{ selectedReview.likeCount }}</dd>
              <dd class="mt-1 text-xs text-slate-500">싫어요 {{ selectedReview.dislikeCount }} · 조회 {{ selectedReview.hits }}</dd>
            </div>
          </dl>

          <div class="mt-6 rounded-[1.5rem] border border-slate-200 bg-slate-50 p-5">
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">리뷰 내용</p>
            <p class="mt-3 whitespace-pre-line leading-7 text-slate-700">{{ selectedReview.content }}</p>
            <p class="mt-4 text-xs text-slate-400">
              작성일 {{ formatDateTime(selectedReview.createdAt) }}
              <span v-if="selectedReview.updatedAt"> · 수정일 {{ formatDateTime(selectedReview.updatedAt) }}</span>
            </p>
          </div>

          <div class="mt-6">
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">첨부 이미지</p>
            <div v-if="selectedReview.imageUrls?.length" class="mt-3 grid grid-cols-2 gap-3">
              <img
                v-for="(imageUrl, index) in selectedReview.imageUrls"
                :key="`${selectedReview.reviewId}-${index}`"
                :src="imageUrl"
                :alt="`리뷰 이미지 ${index + 1}`"
                class="aspect-square w-full rounded-2xl border border-slate-200 object-cover"
              />
            </div>
            <p v-else class="mt-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-500">
              첨부된 리뷰 이미지는 없습니다.
            </p>
          </div>
        </template>

        <div v-else class="flex min-h-[320px] items-center justify-center rounded-[1.5rem] bg-slate-50 px-6 text-center text-sm text-slate-500">
          왼쪽 목록에서 리뷰를 선택하면 상세 내용이 표시됩니다.
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const reviews = ref([])
const selectedReview = ref(null)
const loading = ref(true)
const detailLoading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)

const renderStars = (rating) => `${'★'.repeat(rating)}${'☆'.repeat(Math.max(0, 5 - rating))}`
const formatDateTime = (value) => (value ? new Date(value).toLocaleString('ko-KR') : '-')

async function fetchReviews() {
  loading.value = true
  try {
    const { data } = await adminApi.getAdminReviews({ page: page.value, size: 20 })
    reviews.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || reviews.value.length

    if (!selectedReview.value && reviews.value.length > 0) {
      await selectReview(reviews.value[0].reviewId)
      return
    }

    if (selectedReview.value) {
      const stillExists = reviews.value.some((review) => review.reviewId === selectedReview.value.reviewId)
      if (!stillExists) selectedReview.value = null
    }
  } catch (error) {
    console.error('리뷰 목록 조회 실패:', error)
  } finally {
    loading.value = false
  }
}

async function selectReview(reviewId) {
  detailLoading.value = true
  try {
    const { data } = await adminApi.getAdminReview(reviewId)
    selectedReview.value = data
  } catch (error) {
    console.error('리뷰 상세 조회 실패:', error)
  } finally {
    detailLoading.value = false
  }
}

async function handleDelete(reviewId) {
  if (!confirm('선택한 리뷰를 삭제하시겠습니까?')) return

  try {
    await adminApi.deleteReview(reviewId)
    if (selectedReview.value?.reviewId === reviewId) selectedReview.value = null
    await fetchReviews()
  } catch (error) {
    alert(error.response?.data?.message || '리뷰 삭제에 실패했습니다.')
  }
}

async function changePage(nextPage) {
  page.value = nextPage
  selectedReview.value = null
  await fetchReviews()
}

onMounted(fetchReviews)
</script>
