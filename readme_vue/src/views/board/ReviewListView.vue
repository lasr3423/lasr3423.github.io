<template>
  <section class="space-y-6">

    <!-- ══════════════════════════
         A 섹션 — 상단 카드 그리드 3개
    ══════════════════════════ -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <!-- 헤더 -->
      <div class="border-b border-slate-100 px-6 pt-6 pb-5">
        <p class="text-xs font-semibold uppercase tracking-[0.22em] text-brand-700">Book Review</p>
        <h1 class="mt-2 text-2xl font-black tracking-tight text-slate-900">독자들의 생생한 리뷰</h1>
        <p class="mt-1 text-sm text-slate-400">등록된 리뷰를 확인하고 직접 작성하실 수 있습니다.</p>
      </div>

      <!-- 상품 필터 칩 -->
      <div class="border-b border-slate-100 px-6 py-4">
        <div v-if="productsLoading" class="text-sm text-slate-400">상품 불러오는 중...</div>
        <div v-else class="flex flex-wrap gap-2">
          <button
            v-for="product in products"
            :key="product.id"
            type="button"
            class="rounded-full border px-4 py-1.5 text-sm font-semibold transition"
            :class="selectedProductId === product.id
              ? 'border-brand-800 bg-brand-800 text-white'
              : 'border-slate-200 bg-white text-slate-600 hover:border-brand-300 hover:text-brand-800'"
            @click="changeProduct(product.id)"
          >
            {{ product.title }}
          </button>
        </div>
      </div>

      <!-- 정렬 탭 -->
      <div class="border-b border-slate-100 px-6 py-3 flex gap-2">
        <button
          v-for="tab in heroTabs"
          :key="tab.value"
          type="button"
          class="rounded-full px-4 py-1.5 text-sm font-semibold transition"
          :class="heroTab === tab.value
            ? 'bg-slate-900 text-white'
            : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
          @click="heroTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 카드 3개 -->
      <div class="p-6">
        <div v-if="reviewsLoading" class="py-10 text-center text-sm text-slate-400">불러오는 중...</div>
        <div v-else-if="featuredReviews.length === 0" class="py-10 text-center text-sm text-slate-400">리뷰가 없습니다.</div>
        <div v-else class="grid gap-4 lg:grid-cols-3">
          <article
            v-for="review in featuredReviews"
            :key="`card-${review.reviewId}`"
            class="card-fixed rounded-[1.5rem] border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md cursor-pointer"
            @click="scrollToFeedAndSelect(review.reviewId)"
          >
            <!-- 작성자 + 별점 -->
            <div class="flex items-center gap-3">
              <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-50 text-sm font-bold text-blue-700">
                {{ getInitial(review.memberName) }}
              </div>
              <div class="min-w-0 flex-1">
                <p class="card-title-1 font-semibold text-slate-900 text-sm">{{ review.memberName }}</p>
                <p class="text-xs text-slate-400">{{ formatDate(review.createdAt) }}</p>
              </div>
              <span class="shrink-0 text-sm text-amber-400">{{ renderStars(review.rating) }}</span>
            </div>

            <!-- 책 칩 -->
            <span class="mt-3 inline-block self-start rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
              {{ currentProductTitle }}
            </span>

            <!-- 내용 미리보기 -->
            <p class="card-copy-3 mt-3 flex-1 text-sm leading-6 text-slate-600">{{ review.content }}</p>

            <!-- 푸터 -->
            <div class="mt-4 flex items-center justify-between border-t border-slate-100 pt-3 text-xs text-slate-400">
              <span>👍 {{ review.likeCount ?? 0 }}</span>
              <span>조회 {{ review.hits ?? 0 }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>


    <!-- ══════════════════════════
         C 섹션 — 전체 리뷰 피드
    ══════════════════════════ -->
    <section ref="feedSection" class="space-y-4">
      <!-- 스티키 헤더 -->
      <div class="sticky top-0 z-10 rounded-[1.5rem] border border-slate-200 bg-white/95 px-5 py-4 shadow-sm backdrop-blur">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
          <div class="flex items-center gap-2">
            <span class="rounded-full bg-slate-900 px-3 py-1 text-xs font-bold uppercase tracking-wider text-white">Feed</span>
            <span class="text-sm font-semibold text-slate-700">전체 리뷰 {{ filteredReviews.length }}건</span>
          </div>
          <div class="flex flex-1 gap-2 sm:justify-end">
            <input
              v-model.trim="searchKeyword"
              type="text"
              placeholder="리뷰 내용 또는 작성자 검색"
              class="flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none focus:border-brand-300 focus:bg-white sm:max-w-xs"
            />
            <select
              v-model="sortOption"
              class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-brand-300"
            >
              <option value="latest">최신순</option>
              <option value="rating">별점 높은순</option>
              <option value="likes">좋아요순</option>
              <option value="hits">조회순</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 리뷰 작성 트리거 바 -->
      <button
        type="button"
        class="flex w-full items-center gap-3 rounded-[1.5rem] border border-slate-200 bg-white px-5 py-4 text-left shadow-sm transition hover:border-brand-200"
        @click="toggleWriteForm"
      >
        <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-50 text-sm font-bold text-blue-700">
          {{ authStore.isLoggedIn ? getInitial(authStore.userName || '리뷰') : 'R' }}
        </div>
        <p class="flex-1 text-sm text-slate-400">
          {{ editingReviewId ? '리뷰를 수정 중입니다...' : '이 책에 대한 생각을 남겨보세요...' }}
        </p>
        <span class="shrink-0 rounded-full bg-slate-900 px-4 py-1.5 text-xs font-semibold text-white">
          {{ showWriteForm ? '닫기' : '작성하기' }}
        </span>
      </button>

      <!-- 리뷰 작성 폼 -->
      <section v-if="showWriteForm || editingReviewId" class="rounded-[1.8rem] border border-slate-200 bg-white p-6 shadow-sm">
        <div class="mb-5 flex items-start justify-between gap-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.22em] text-brand-700">{{ editingReviewId ? 'Edit Review' : 'Write Review' }}</p>
            <h3 class="mt-2 text-xl font-black text-slate-900">{{ currentProductTitle }}</h3>
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
                  : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200'"
                @click="reviewForm.rating = score"
              >{{ score }}점</button>
            </div>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">리뷰 내용</span>
            <textarea
              v-model="reviewForm.content"
              class="min-h-32 w-full rounded-[1.4rem] border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-300 focus:bg-white"
              placeholder="상품을 읽고 느낀 점을 편하게 적어 주세요"
            ></textarea>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">이미지 URL</span>
            <textarea
              v-model="reviewForm.imageUrlText"
              class="min-h-20 w-full rounded-[1.4rem] border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-300 focus:bg-white"
              placeholder="이미지 URL (여러 개면 줄바꿈으로 구분)"
            ></textarea>
          </label>

          <p v-if="formMessage" class="rounded-2xl px-4 py-3 text-sm" :class="formMessageClass">{{ formMessage }}</p>

          <div class="flex justify-end gap-3">
            <button v-if="editingReviewId" type="button"
              class="rounded-full border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              @click="cancelReviewEdit"
            >수정 취소</button>
            <button type="submit"
              class="rounded-full bg-brand-800 px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:opacity-50"
              :disabled="!authStore.isLoggedIn || reviewSubmitting || !selectedProductId"
            >{{ reviewSubmitting ? '저장 중...' : (editingReviewId ? '리뷰 수정' : '리뷰 등록') }}</button>
          </div>
        </form>
      </section>

      <!-- 피드 로딩 -->
      <div v-if="reviewsLoading" class="rounded-[1.5rem] border border-slate-200 bg-white py-14 text-center text-sm text-slate-400">
        리뷰를 불러오는 중입니다...
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="filteredReviews.length === 0" class="rounded-[1.5rem] border border-slate-200 bg-white py-14 text-center text-sm text-slate-400">
        검색 결과가 없거나 등록된 리뷰가 없습니다.
      </div>

      <!-- 피드 아이템 -->
      <article
        v-for="review in filteredReviews"
        v-else
        :key="review.reviewId"
        :ref="el => { if (el) feedItemRefs[review.reviewId] = el }"
        class="flex gap-4 rounded-[1.5rem] border bg-white px-5 py-5 shadow-sm transition"
        :class="selectedReview?.reviewId === review.reviewId
          ? 'border-brand-200 bg-brand-50/40'
          : 'border-slate-200'"
      >
        <!-- 책 썸네일 -->
        <div class="hidden h-20 w-14 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-[10px] font-semibold text-slate-400 sm:flex">
          BOOK
        </div>

        <!-- 본문 -->
        <div class="min-w-0 flex-1">
          <p class="mb-2 text-xs font-semibold text-blue-700">{{ currentProductTitle }}</p>

          <div class="flex items-center gap-2 flex-wrap">
            <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-blue-50 text-xs font-bold text-blue-700">
              {{ getInitial(review.memberName) }}
            </div>
            <span class="font-semibold text-slate-900 text-sm">{{ review.memberName }}</span>
            <span class="text-sm text-amber-400">{{ renderStars(review.rating) }}</span>
            <span class="ml-auto text-xs text-slate-400">{{ formatDate(review.createdAt) }}</span>
          </div>

          <p class="mt-3 text-sm leading-7 text-slate-700 whitespace-pre-line">{{ review.content }}</p>

          <!-- 이미지 (있을 경우) -->
          <div v-if="review.imageUrls?.length" class="mt-3 flex gap-2">
            <img
              v-for="(url, i) in review.imageUrls.slice(0, 3)"
              :key="i"
              :src="url"
              class="h-16 w-16 rounded-xl border border-slate-200 object-cover"
            />
          </div>

          <!-- 액션 버튼 -->
          <div class="mt-4 flex flex-wrap items-center gap-2">
            <button
              class="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-1.5 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
              @click="reactFeed(review.reviewId, 'LIKE')"
            >👍 좋아요 {{ review.likeCount ?? 0 }}</button>
            <button
              class="rounded-full border border-slate-200 bg-slate-50 px-4 py-1.5 text-xs font-semibold text-slate-500 transition hover:bg-slate-100"
              @click="reactFeed(review.reviewId, 'DISLIKE')"
            >👎 별로예요 {{ review.dislikeCount ?? 0 }}</button>
            <span class="ml-auto text-xs text-slate-400">조회 {{ review.hits ?? 0 }}</span>

            <!-- 본인 리뷰면 수정/삭제 -->
            <template v-if="review.memberId === currentMemberId">
              <button
                class="rounded-full border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:bg-slate-50"
                @click="startEditReviewById(review)"
              >수정</button>
              <button
                class="rounded-full bg-rose-500 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-rose-600"
                @click="deleteReviewById(review.reviewId)"
              >삭제</button>
            </template>
          </div>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { productApi } from '@/api/product'
import { reviewApi } from '@/api/board'
import { memberApi } from '@/api/member'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const products = ref([])
const selectedProductId = ref(null)
const reviews = ref([])
const selectedReview = ref(null)
const currentMemberId = ref(null)
const productsLoading = ref(true)
const reviewsLoading = ref(true)
const reviewSubmitting = ref(false)
const editingReviewId = ref(null)
const formMessage = ref('')
const formMessageClass = ref('bg-emerald-50 text-emerald-700')
const heroTab = ref('all')
const searchKeyword = ref('')
const sortOption = ref('latest')
const showWriteForm = ref(false)
const feedSection = ref(null)
const feedItemRefs = reactive({})

const heroTabs = [
  { value: 'all',    label: '전체' },
  { value: 'best',   label: '베스트' },
  { value: 'latest', label: '최신순' },
  { value: 'rating', label: '별점순' },
]

const reviewForm = reactive({ rating: 5, content: '', imageUrlText: '' })

const currentProductTitle = computed(() =>
  products.value.find(p => p.id === selectedProductId.value)?.title || '선택한 상품'
)

const heroSourceReviews = computed(() => {
  const cloned = [...reviews.value]
  switch (heroTab.value) {
    case 'best':   return cloned.sort((a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0))
    case 'latest': return cloned.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    case 'rating': return cloned.sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0))
    default:       return cloned
  }
})

const featuredReviews = computed(() => heroSourceReviews.value.slice(0, 3))

const filteredReviews = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  let list = [...reviews.value]
  if (kw) {
    list = list.filter(r =>
      [r.memberName, r.content].filter(Boolean).some(v => String(v).toLowerCase().includes(kw))
    )
  }
  switch (sortOption.value) {
    case 'rating': list.sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0)); break
    case 'likes':  list.sort((a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0)); break
    case 'hits':   list.sort((a, b) => (b.hits ?? 0) - (a.hits ?? 0)); break
    default:       list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)); break
  }
  return list
})

async function initialize() {
  if (authStore.isLoggedIn) {
    try {
      const { data } = await memberApi.getMe()
      currentMemberId.value = data.id
    } catch (e) { console.error(e) }
  }
  await fetchProducts()
}

async function fetchProducts() {
  productsLoading.value = true
  try {
    const { data } = await productApi.getList({ page: 0, size: 10 })
    products.value = data.content ?? []
    if (products.value[0]) {
      selectedProductId.value = products.value[0].id
      await fetchReviews()
    }
  } catch (e) {
    console.error(e)
    products.value = []
  } finally {
    productsLoading.value = false
  }
}

async function fetchReviews() {
  if (!selectedProductId.value) return
  reviewsLoading.value = true
  selectedReview.value = null
  try {
    const { data } = await reviewApi.getList({ productId: selectedProductId.value, page: 0, size: 50, sort: 'createdAt,desc' })
    reviews.value = data.content ?? []
  } catch (e) {
    console.error(e)
    reviews.value = []
  } finally {
    reviewsLoading.value = false
  }
}

async function changeProduct(productId) {
  selectedProductId.value = productId
  searchKeyword.value = ''
  sortOption.value = 'latest'
  cancelReviewEdit()
  showWriteForm.value = false
  await fetchReviews()
}

function scrollToFeedAndSelect(reviewId) {
  selectedReview.value = reviews.value.find(r => r.reviewId === reviewId) || null
  const el = feedItemRefs[reviewId]
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function toggleWriteForm() {
  if (!authStore.isLoggedIn && !editingReviewId.value) {
    alert('로그인 후 리뷰를 작성할 수 있습니다.')
    return
  }
  showWriteForm.value = !showWriteForm.value
}

function parseImageUrls() {
  return reviewForm.imageUrlText.split('\n').map(v => v.trim()).filter(Boolean)
}

async function submitReview() {
  if (!authStore.isLoggedIn) { formMessage.value = '로그인이 필요합니다.'; formMessageClass.value = 'bg-amber-50 text-amber-700'; return }
  if (!reviewForm.content.trim()) { formMessage.value = '리뷰 내용을 입력해 주세요.'; formMessageClass.value = 'bg-rose-50 text-rose-700'; return }

  reviewSubmitting.value = true
  try {
    const payload = { productId: selectedProductId.value, rating: reviewForm.rating, content: reviewForm.content.trim(), imageUrls: parseImageUrls() }
    if (editingReviewId.value) {
      await reviewApi.updateMy(editingReviewId.value, { rating: payload.rating, content: payload.content, imageUrls: payload.imageUrls })
      formMessage.value = '리뷰가 수정되었습니다.'
    } else {
      await reviewApi.create(payload)
      formMessage.value = '리뷰가 등록되었습니다.'
    }
    formMessageClass.value = 'bg-emerald-50 text-emerald-700'
    cancelReviewEdit()
    showWriteForm.value = false
    await fetchReviews()
  } catch (e) {
    formMessage.value = e.response?.data?.message || '저장에 실패했습니다.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    reviewSubmitting.value = false
  }
}

function startEditReviewById(review) {
  editingReviewId.value = review.reviewId
  reviewForm.rating = review.rating
  reviewForm.content = review.content
  reviewForm.imageUrlText = (review.imageUrls || []).join('\n')
  formMessage.value = '수정 모드입니다.'
  formMessageClass.value = 'bg-sky-50 text-sky-700'
  showWriteForm.value = true
  feedSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function cancelReviewEdit() {
  editingReviewId.value = null
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.imageUrlText = ''
  formMessage.value = ''
}

async function deleteReviewById(reviewId) {
  if (!confirm('이 리뷰를 삭제하시겠습니까?')) return
  try {
    await reviewApi.removeMy(reviewId)
    await fetchReviews()
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  }
}

async function reactFeed(reviewId, type) {
  if (!authStore.isLoggedIn) { alert('로그인 후 반응을 남길 수 있습니다.'); return }
  try {
    await reviewApi.react(reviewId, type)
    await fetchReviews()
  } catch (e) {
    console.error(e)
  }
}

function getInitial(name) { return String(name || 'R').trim().charAt(0).toUpperCase() }
function renderStars(r) { const n = Math.max(0, Math.min(5, Number(r) || 0)); return '★'.repeat(n) + '☆'.repeat(5 - n) }
function formatDate(v) {
  if (!v) return '-'
  return new Date(v).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(initialize)
</script>
