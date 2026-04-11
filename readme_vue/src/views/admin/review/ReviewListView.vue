<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="point-chip">관리자 리뷰</p>
          <h1 class="section-title mt-3">상품 리뷰 목록을 확인하고 관리하세요</h1>
          <p class="mt-2 text-sm text-slate-500">
            상품 제목, 리뷰 내용, 작성자 기준으로 검색할 수 있고 선택한 리뷰를 한 번에 삭제할 수 있습니다.
          </p>
        </div>
        <button
          class="rounded-xl bg-rose-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="selectedIds.length === 0"
          @click="removeSelected"
        >
          삭제하기
        </button>
      </div>
    </section>

    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-3 lg:flex-row">
        <select
          v-model="searchType"
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100 lg:w-52"
        >
          <option value="productTitle">상품 제목</option>
          <option value="reviewTitle">리뷰 내용</option>
          <option value="memberName">리뷰 작성자</option>
        </select>
        <input
          v-model.trim="keyword"
          type="text"
          placeholder="검색어를 입력하세요"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          @keyup.enter="search"
        >
        <button
          class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          @click="search"
        >
          검색
        </button>
      </div>
    </section>

    <section class="surface-panel overflow-hidden rounded-[2rem]">
      <div v-if="loading" class="p-12 text-center text-sm text-slate-500">리뷰 목록을 불러오는 중입니다...</div>
      <div v-else-if="errorMessage" class="p-12 text-center text-sm text-rose-500">{{ errorMessage }}</div>

      <template v-else-if="reviews.length > 0">
        <table class="w-full text-sm">
          <thead class="border-b border-slate-200 bg-slate-50 text-slate-500">
            <tr>
              <th class="w-10 px-4 py-4 text-center font-semibold">
                <input
                  type="checkbox"
                  :checked="allChecked"
                  class="h-4 w-4 rounded border-slate-300"
                  @change="toggleAll"
                >
              </th>
              <th class="w-16 px-5 py-4 text-center font-semibold">ID</th>
              <th class="w-48 px-5 py-4 text-center font-semibold">상품명</th>
              <th class="px-5 py-4 text-center font-semibold">리뷰내용</th>
              <th class="w-28 px-5 py-4 text-center font-semibold">작성자</th>
              <th class="w-28 px-5 py-4 text-center font-semibold">평점</th>
              <th class="w-32 px-5 py-4 text-center font-semibold">작성일</th>
              <th class="w-28 px-5 py-4 text-center font-semibold">관리</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="review in reviews" :key="review.reviewId" class="transition hover:bg-slate-50">
              <!-- 체크박스 -->
              <td class="px-4 py-4 text-center">
                <input
                  type="checkbox"
                  :checked="selectedIds.includes(review.reviewId)"
                  class="h-4 w-4 rounded border-slate-300"
                  @change="toggleOne(review.reviewId)"
                >
              </td>

              <!-- ID -->
              <td class="px-5 py-4 text-center text-xs text-slate-400">
                {{ review.reviewId }}
              </td>

              <!-- 상품명 -->
              <td class="px-5 py-4 text-center">
                <p class="font-medium text-slate-900 line-clamp-1">
                  {{ review.productTitle || `상품 #${review.productId}` }}
                </p>
              </td>

              <!-- 리뷰내용 (10자 맛보기) -->
              <td class="px-5 py-4 text-center text-slate-600">
                {{ review.content ? review.content.slice(0, 10) + (review.content.length > 10 ? '…' : '') : '-' }}
              </td>

              <!-- 작성자 -->
              <td class="px-5 py-4 text-center text-slate-800">
                {{ review.memberName }}
              </td>

              <!-- 평점 -->
              <td class="px-5 py-4 text-center">
                <span class="text-amber-400 text-base">{{ renderStars(review.rating) }}</span>
              </td>

              <!-- 작성일 -->
              <td class="px-5 py-4 text-center text-slate-500">
                {{ formatDate(review.createdAt) }}
              </td>

              <!-- 관리 -->
              <td class="px-5 py-4 text-center">
                <div class="inline-flex gap-2">
                  <button
                    type="button"
                    class="whitespace-nowrap rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
                    @click="openDetail(review.reviewId)"
                  >상세</button>
                  <button
                    type="button"
                    class="whitespace-nowrap rounded-xl bg-rose-500 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-rose-600"
                    @click="handleDelete(review.reviewId)"
                  >삭제</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button
            type="button"
            :disabled="page === 0"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40"
            @click="changePage(page - 1)"
          >
            이전
          </button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            type="button"
            :disabled="page >= totalPages - 1"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40"
            @click="changePage(page + 1)"
          >
            다음
          </button>
        </div>
      </template>

      <div v-else class="p-12 text-center text-sm text-slate-500">등록된 리뷰가 없습니다.</div>
    </section>

    <div
      v-if="detailOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4"
      @click.self="closeDetail"
    >
      <div class="w-full max-w-3xl rounded-[2rem] bg-white p-6 shadow-2xl">
        <template v-if="detailLoading">
          <div class="flex min-h-[320px] items-center justify-center text-sm text-slate-400">
            리뷰 상세를 불러오는 중입니다.
          </div>
        </template>

        <template v-else-if="selectedReview">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Review Detail</p>
              <h2 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">{{ reviewTitle(selectedReview) }}</h2>
            </div>
            <button
              type="button"
              class="rounded-full border border-slate-200 px-3 py-1 text-sm text-slate-500 transition hover:bg-slate-50"
              @click="closeDetail"
            >
              닫기
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
            <p class="mt-3 whitespace-pre-line break-words leading-7 text-slate-700">{{ selectedReview.content }}</p>
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
              >
            </div>
            <p v-else class="mt-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-500">
              첨부된 리뷰 이미지가 없습니다.
            </p>
          </div>

          <div class="mt-6 flex justify-end gap-3">
            <button
              type="button"
              class="rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              @click="closeDetail"
            >
              닫기
            </button>
            <button
              type="button"
              class="rounded-xl bg-rose-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-rose-600"
              @click="handleDelete(selectedReview.reviewId)"
            >
              리뷰 삭제
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const reviews = ref([])
const selectedReview = ref(null)
const loading = ref(true)
const detailLoading = ref(false)
const detailOpen = ref(false)
const page = ref(0)
const totalPages = ref(1)
const keyword = ref('')
const searchType = ref('productTitle')
const selectedIds = ref([])
const errorMessage = ref('')

const allChecked = computed(() => {
  if (!reviews.value.length) return false
  return reviews.value.every((review) => selectedIds.value.includes(review.reviewId))
})

const renderStars = (rating) => `${'★'.repeat(rating)}${'☆'.repeat(Math.max(0, 5 - rating))}`
const formatDateTime = (value) => (value ? new Date(value).toLocaleString('ko-KR') : '-')
const formatDate = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '-')
const formatTime = (value) => (value ? new Date(value).toLocaleTimeString('ko-KR') : '-')

const reviewTitle = (review) => {
  const source = review?.content?.trim() || ''
  if (!source) return `리뷰 #${review.reviewId}`
  return source.length > 42 ? `${source.slice(0, 42)}...` : source
}

async function fetchReviews() {
  loading.value = true
  errorMessage.value = ''

  try {
    const params = {
      page: page.value,
      size: 20,
      searchType: searchType.value,
    }

    if (keyword.value) {
      params.keyword = keyword.value
    }

    const { data } = await adminApi.getAdminReviews(params)
    reviews.value = data.content ?? []
    totalPages.value = Math.max(data.totalPages || 0, 1)
    selectedIds.value = selectedIds.value.filter((id) => reviews.value.some((review) => review.reviewId === id))
  } catch (error) {
    console.error('리뷰 목록 조회 실패:', error)
    reviews.value = []
    errorMessage.value = error.response?.data?.message || '리뷰 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function openDetail(reviewId) {
  detailOpen.value = true
  detailLoading.value = true

  try {
    const { data } = await adminApi.getAdminReview(reviewId)
    selectedReview.value = data
  } catch (error) {
    console.error('리뷰 상세 조회 실패:', error)
    selectedReview.value = null
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  detailOpen.value = false
}

function toggleOne(reviewId) {
  if (selectedIds.value.includes(reviewId)) {
    selectedIds.value = selectedIds.value.filter((id) => id !== reviewId)
    return
  }
  selectedIds.value = [...selectedIds.value, reviewId]
}

function toggleAll(event) {
  selectedIds.value = event.target.checked ? reviews.value.map((review) => review.reviewId) : []
}

async function handleDelete(reviewId) {
  if (!confirm('선택한 리뷰를 삭제하시겠어요?')) return

  try {
    await adminApi.deleteReview(reviewId)
    selectedIds.value = selectedIds.value.filter((id) => id !== reviewId)
    if (selectedReview.value?.reviewId === reviewId) {
      detailOpen.value = false
      selectedReview.value = null
    }
    await fetchReviews()
  } catch (error) {
    alert(error.response?.data?.message || '리뷰 삭제에 실패했습니다.')
  }
}

async function removeSelected() {
  if (!selectedIds.value.length) return
  if (!confirm(`선택한 리뷰 ${selectedIds.value.length}건을 삭제하시겠어요?`)) return

  try {
    await Promise.all(selectedIds.value.map((id) => adminApi.deleteReview(id)))
    selectedIds.value = []
    detailOpen.value = false
    selectedReview.value = null
    await fetchReviews()
  } catch (error) {
    alert(error.response?.data?.message || '리뷰 일괄 삭제에 실패했습니다.')
  }
}

async function changePage(nextPage) {
  page.value = nextPage
  await fetchReviews()
}

async function search() {
  page.value = 0
  await fetchReviews()
}

onMounted(fetchReviews)
</script>
