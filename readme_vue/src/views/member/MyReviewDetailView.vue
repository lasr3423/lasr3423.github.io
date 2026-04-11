<template>
  <div class="space-y-6">

    <!-- 헤더 -->
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="point-chip">내 리뷰 상세</p>
          <h1 class="section-title mt-3">리뷰 상세 보기</h1>
        </div>
        <button
          class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
          @click="router.push('/mypage/review')"
        >
          ← 목록으로
        </button>
      </div>
    </section>

    <!-- 로딩 -->
    <div v-if="loading" class="surface-panel rounded-[2rem] px-6 py-20 text-center text-sm text-slate-400">
      리뷰를 불러오는 중입니다.
    </div>

    <!-- 에러 -->
    <div v-else-if="error" class="surface-panel rounded-[2rem] px-6 py-20 text-center">
      <p class="text-sm text-rose-500">{{ error }}</p>
      <button class="mt-4 rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700" @click="router.push('/mypage/review')">
        목록으로 돌아가기
      </button>
    </div>

    <!-- 본문 -->
    <template v-else-if="review">

      <!-- 리뷰 카드 -->
      <section class="surface-panel rounded-[2rem] p-6">

        <!-- 상품명 + 평점 -->
        <div class="mb-5">
          <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-700">상품명</p>
          <p class="mt-2 text-xl font-bold text-slate-900">{{ review.productTitle }}</p>
          <div class="mt-2 flex items-center gap-3">
            <span class="point-chip">{{ renderStars(review.rating) }}</span>
            <span class="text-sm text-slate-500">{{ formatDate(review.createdAt) }}</span>
          </div>
        </div>

        <!-- 수정 폼 -->
        <form v-if="editing" class="space-y-4" @submit.prevent="submitEdit">
          <div>
            <p class="mb-2 text-sm font-medium text-slate-700">평점 수정</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="score in [1, 2, 3, 4, 5]"
                :key="score"
                type="button"
                class="rounded-full border px-4 py-2 text-sm font-semibold transition"
                :class="editForm.rating === score
                  ? 'border-brand-800 bg-brand-800 text-white'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-brand-200'"
                @click="editForm.rating = score"
              >{{ score }}점</button>
            </div>
          </div>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">리뷰 내용</span>
            <textarea v-model="editForm.content" class="surface-soft min-h-28 w-full rounded-xl px-4 py-3 text-sm outline-none"></textarea>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">이미지 URL (줄바꿈으로 구분)</span>
            <textarea v-model="editForm.imageUrlText" class="surface-soft min-h-20 w-full rounded-xl px-4 py-3 text-sm outline-none" placeholder="이미지 URL을 입력해 주세요"></textarea>
          </label>

          <p v-if="formMessage" class="rounded-xl px-4 py-3 text-sm" :class="formMessageClass">{{ formMessage }}</p>

          <div class="flex gap-3">
            <button type="button" class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50" @click="cancelEdit">
              취소
            </button>
            <button type="submit" class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400" :disabled="editSubmitting">
              {{ editSubmitting ? '저장 중...' : '저장' }}
            </button>
          </div>
        </form>

        <!-- 읽기 모드 -->
        <template v-else>
          <div class="rounded-[1.5rem] bg-slate-50 px-5 py-5 text-sm leading-7 text-slate-700 whitespace-pre-line">
            {{ review.content }}
          </div>

          <!-- 이미지 -->
          <div v-if="review.imageUrls?.length" class="mt-4 grid grid-cols-2 gap-3">
            <img
              v-for="(url, i) in review.imageUrls"
              :key="i"
              :src="url"
              :alt="`리뷰 이미지 ${i + 1}`"
              class="aspect-square w-full rounded-2xl border border-slate-200 object-cover"
            >
          </div>

          <!-- 좋아요/별로 -->
          <div class="mt-4 grid grid-cols-2 gap-3">
            <div class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-center text-sm font-semibold text-emerald-700">
              좋아요 {{ review.likeCount ?? 0 }}
            </div>
            <div class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-center text-sm font-semibold text-rose-700">
              별로예요 {{ review.dislikeCount ?? 0 }}
            </div>
          </div>

          <!-- 수정/삭제 버튼 -->
          <div class="mt-5 flex gap-3">
            <button
              class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              @click="startEdit"
            >
              리뷰 수정
            </button>
            <button
              class="rounded-xl bg-rose-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:bg-slate-300"
              :disabled="deleting"
              @click="deleteReview"
            >
              {{ deleting ? '삭제 중...' : '리뷰 삭제' }}
            </button>
          </div>
        </template>
      </section>

    </template>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reviewApi } from '@/api/board'

const route = useRoute()
const router = useRouter()

const review = ref(null)
const loading = ref(true)
const error = ref('')
const editing = ref(false)
const editSubmitting = ref(false)
const deleting = ref(false)
const formMessage = ref('')
const formMessageClass = ref('bg-emerald-50 text-emerald-700')

const editForm = reactive({ rating: 5, content: '', imageUrlText: '' })

// ─── 상세 조회 ─────────────────────────────────────────────────────────────
async function fetchDetail() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await reviewApi.getMyDetail(route.params.id)
    review.value = data
  } catch (e) {
    error.value = e.response?.data?.message || '리뷰를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

// ─── 수정 ─────────────────────────────────────────────────────────────────
function startEdit() {
  editForm.rating = review.value.rating
  editForm.content = review.value.content
  editForm.imageUrlText = (review.value.imageUrls || []).join('\n')
  editing.value = true
  formMessage.value = ''
}

function cancelEdit() {
  editing.value = false
  formMessage.value = ''
}

async function submitEdit() {
  if (!editForm.content.trim()) {
    formMessage.value = '리뷰 내용을 입력해 주세요.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
    return
  }
  editSubmitting.value = true
  try {
    await reviewApi.updateMy(review.value.reviewId, {
      rating: editForm.rating,
      content: editForm.content.trim(),
      imageUrls: editForm.imageUrlText
        .split('\n')
        .map((v) => v.trim())
        .filter(Boolean),
    })
    editing.value = false
    formMessage.value = ''
    await fetchDetail()
  } catch (e) {
    formMessage.value = e.response?.data?.message || '수정에 실패했습니다.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    editSubmitting.value = false
  }
}

// ─── 삭제 ─────────────────────────────────────────────────────────────────
async function deleteReview() {
  if (!confirm('이 리뷰를 삭제하시겠습니까?')) return
  deleting.value = true
  try {
    await reviewApi.removeMy(review.value.reviewId)
    router.push('/mypage/review')
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
    deleting.value = false
  }
}

// ─── 유틸 ─────────────────────────────────────────────────────────────────
function renderStars(rating) {
  return '★'.repeat(rating ?? 0) + '☆'.repeat(5 - (rating ?? 0))
}

function formatDate(v) {
  if (!v) return '-'
  return new Date(v).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(fetchDetail)
</script>
