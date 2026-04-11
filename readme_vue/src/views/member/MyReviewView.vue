<template>
  <div class="space-y-6">

    <!-- 헤더 -->
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="point-chip">내 리뷰 관리</p>
          <h1 class="section-title mt-3">내가 작성한 리뷰</h1>
          <p class="mt-2 text-sm text-slate-500">내가 남긴 상품 리뷰를 확인하고 관리하세요.</p>
        </div>
      </div>
    </section>

    <!-- 리스트 -->
    <section class="surface-panel rounded-[2rem] overflow-hidden">
      <!-- 리스트 툴바 -->
      <div class="flex items-center justify-between gap-4 border-b border-slate-100 px-6 py-4">
        <div class="flex items-center gap-3">
          <input
            type="checkbox"
            class="h-4 w-4 rounded border-slate-300 text-brand-700"
            :checked="isAllChecked"
            :indeterminate.prop="isSomeChecked"
            @change="toggleAll"
          >
          <span class="text-sm text-slate-500">
            {{ checkedIds.size > 0 ? `${checkedIds.size}건 선택됨` : `전체 ${reviews.length}건` }}
          </span>
        </div>
        <button
          v-if="checkedIds.size > 0"
          class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:bg-slate-300"
          :disabled="deleting"
          @click="deleteSelected"
        >
          {{ deleting ? '삭제 중...' : `선택 삭제 (${checkedIds.size})` }}
        </button>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="px-6 py-16 text-center text-sm text-slate-400">목록을 불러오는 중입니다.</div>

      <!-- 빈 상태 -->
      <div v-else-if="reviews.length === 0" class="px-6 py-16 text-center">
        <p class="text-sm text-slate-500">작성한 리뷰가 없습니다.</p>
      </div>

      <!-- 리뷰 행 -->
      <ul v-else class="divide-y divide-slate-100">
        <li
          v-for="review in reviews"
          :key="review.reviewId"
          class="flex items-start gap-4 px-6 py-4 transition hover:bg-slate-50"
        >
          <!-- 체크박스 -->
          <input
            type="checkbox"
            class="mt-1 h-4 w-4 shrink-0 rounded border-slate-300 text-brand-700"
            :checked="checkedIds.has(review.reviewId)"
            @change="toggleItem(review.reviewId)"
            @click.stop
          >

          <!-- 클릭 영역 → 상세 이동 -->
          <div class="min-w-0 flex-1 cursor-pointer" @click="goDetail(review.reviewId)">
            <div class="flex flex-wrap items-center gap-2">
              <span class="point-chip shrink-0">{{ renderStars(review.rating) }}</span>
              <span class="truncate text-sm font-semibold text-slate-900">{{ review.productTitle }}</span>
            </div>
            <p class="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">{{ review.content }}</p>
            <div class="mt-1.5 flex items-center gap-3 text-xs text-slate-400">
              <span>{{ formatDate(review.createdAt) }}</span>
              <span>조회 {{ review.hits ?? 0 }}</span>
            </div>
          </div>

          <!-- 상세 보기 화살표 -->
          <button
            class="shrink-0 rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-brand-200 hover:text-brand-700"
            @click="goDetail(review.reviewId)"
          >
            상세 →
          </button>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { reviewApi } from '@/api/board'

const router = useRouter()

const reviews = ref([])
const loading = ref(true)
const deleting = ref(false)
const checkedIds = ref(new Set())

// ─── 전체/부분 선택 상태 ────────────────────────────────────────────────────
const isAllChecked = computed(() => reviews.value.length > 0 && checkedIds.value.size === reviews.value.length)
const isSomeChecked = computed(() => checkedIds.value.size > 0 && checkedIds.value.size < reviews.value.length)

function toggleAll(e) {
  if (e.target.checked) {
    checkedIds.value = new Set(reviews.value.map((r) => r.reviewId))
  } else {
    checkedIds.value = new Set()
  }
}

function toggleItem(id) {
  const next = new Set(checkedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  checkedIds.value = next
}

// ─── 목록 조회 ─────────────────────────────────────────────────────────────
async function fetchMyReviews() {
  loading.value = true
  try {
    const { data } = await reviewApi.getMyList({ page: 0, size: 100, sort: 'createdAt,desc' })
    reviews.value = data.content ?? []
    checkedIds.value = new Set()
  } catch (e) {
    console.error('내 리뷰 목록 조회 실패', e)
    reviews.value = []
  } finally {
    loading.value = false
  }
}

// ─── 상세 이동 ─────────────────────────────────────────────────────────────
function goDetail(id) {
  router.push(`/mypage/review/detail/${id}`)
}

// ─── 선택 삭제 ─────────────────────────────────────────────────────────────
async function deleteSelected() {
  if (checkedIds.value.size === 0) return
  if (!confirm(`선택한 ${checkedIds.value.size}건의 리뷰를 삭제하시겠습니까?`)) return

  deleting.value = true
  const ids = [...checkedIds.value]
  const results = await Promise.allSettled(ids.map((id) => reviewApi.removeMy(id)))
  const failed = results.filter((r) => r.status === 'rejected').length

  if (failed > 0) {
    alert(`${ids.length - failed}건 삭제 완료, ${failed}건 실패하였습니다.`)
  }

  deleting.value = false
  await fetchMyReviews()
}

// ─── 유틸 ─────────────────────────────────────────────────────────────────
function renderStars(rating) {
  return '★'.repeat(rating ?? 0) + '☆'.repeat(5 - (rating ?? 0))
}

function formatDate(v) {
  if (!v) return '-'
  return new Date(v).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(fetchMyReviews)
</script>
