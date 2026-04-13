<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="point-chip">내 리뷰 관리</p>
          <h1 class="section-title mt-3">내 리뷰</h1>
          <p class="mt-2 text-sm text-slate-500">작성한 리뷰를 목록에서 확인하고 상세 내용은 모달에서 보실 수 있습니다.</p>
        </div>
        <button
          class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          @click="openWriteGuide"
        >
          리뷰 작성 안내
        </button>
      </div>
    </section>

    <section class="surface-panel overflow-hidden rounded-[2rem]">
      <div class="flex flex-col gap-4 border-b border-slate-100 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">
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
        <div class="flex items-center justify-end gap-2">
          <button
            v-if="checkedIds.size > 0"
            class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:bg-slate-300"
            :disabled="deleting"
            @click="deleteSelected"
          >
            {{ deleting ? '삭제 중...' : `선택 삭제 (${checkedIds.size})` }}
          </button>
          <button
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="openWriteGuide"
          >
            리뷰 작성 안내
          </button>
        </div>
      </div>

      <div v-if="loading" class="px-6 py-16 text-center text-sm text-slate-400">목록을 불러오는 중입니다.</div>

      <div v-else-if="reviews.length === 0" class="px-6 py-16 text-center">
        <p class="text-sm text-slate-500">작성한 리뷰가 없습니다.</p>
        <button class="mt-4 rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700" @click="openWriteGuide">
          리뷰 작성 방법 보기
        </button>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[820px] table-fixed text-sm">
          <colgroup>
            <col class="w-14">
            <col class="w-16">
            <col class="w-20">
            <col class="w-52">
            <col>
            <col class="w-28">
            <col class="w-24">
          </colgroup>
          <thead class="border-b border-slate-200 bg-slate-50">
            <tr class="text-slate-500">
              <th class="px-4 py-3 text-center font-semibold">선택</th>
              <th class="px-4 py-3 text-center font-semibold">No</th>
              <th class="px-4 py-3 text-center font-semibold">평점</th>
              <th class="px-4 py-3 text-left font-semibold">도서명</th>
              <th class="px-4 py-3 text-left font-semibold">리뷰 내용</th>
              <th class="px-4 py-3 text-center font-semibold">작성일</th>
              <th class="px-4 py-3 text-center font-semibold">보기</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="(review, index) in reviews"
              :key="review.reviewId"
              class="transition hover:bg-slate-50"
            >
              <td class="px-4 py-4 text-center">
                <input
                  type="checkbox"
                  class="h-4 w-4 rounded border-slate-300 text-brand-700"
                  :checked="checkedIds.has(review.reviewId)"
                  @change="toggleItem(review.reviewId)"
                >
              </td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ reviews.length - index }}</td>
              <td class="px-4 py-4 text-center">
                <span class="inline-flex justify-center rounded-full bg-accent-50 px-2.5 py-1 text-xs font-semibold text-accent-600">
                  {{ renderStars(review.rating) }}
                </span>
              </td>
              <td class="px-4 py-4">
                <button
                  type="button"
                  class="card-title-1 block w-full text-left font-medium text-slate-900 hover:text-brand-800"
                  @click="openDetailModal(review.reviewId)"
                >
                  {{ review.productTitle }}
                </button>
              </td>
              <td class="px-4 py-4">
                <button
                  type="button"
                  class="card-title-1 block w-full text-left text-slate-600 hover:text-brand-800"
                  @click="openDetailModal(review.reviewId)"
                >
                  {{ review.content }}
                </button>
              </td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ formatDate(review.createdAt) }}</td>
              <td class="px-4 py-4 text-center">
                <button
                  type="button"
                  class="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-brand-200 hover:text-brand-700"
                  @click="openDetailModal(review.reviewId)"
                >
                  상세
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div
      v-if="modalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 px-4 py-8 backdrop-blur-sm"
      @click.self="closeModal"
    >
      <section class="surface-panel max-h-[90vh] w-full max-w-3xl overflow-hidden rounded-[2rem]">
        <header class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
          <div>
            <p class="point-chip">{{ modalMode === 'guide' ? '리뷰 작성 안내' : modalMode === 'edit' ? '리뷰 수정' : '리뷰 상세' }}</p>
            <h2 class="mt-3 text-xl font-bold text-slate-900">
              {{
                modalMode === 'guide'
                  ? '리뷰 작성 방법'
                  : modalMode === 'edit'
                    ? '리뷰 수정'
                    : activeReview?.productTitle || '리뷰 상세'
              }}
            </h2>
          </div>
          <button
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
            @click="closeModal"
          >
            닫기
          </button>
        </header>

        <div class="max-h-[calc(90vh-104px)] overflow-y-auto px-6 py-6">
          <div v-if="modalMode === 'guide'" class="space-y-4">
            <div class="rounded-[1.5rem] bg-slate-50 px-5 py-5 text-sm leading-7 text-slate-600">
              배송이 완료된 주문 상세에서 리뷰를 작성하실 수 있습니다.
              <br>
              주문 내역에서 배송 완료 주문을 선택한 뒤 리뷰 작성 버튼을 이용해 주세요.
            </div>
            <div class="flex justify-end">
              <button
                class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700"
                @click="goOrderPage"
              >
                주문 내역 보기
              </button>
            </div>
          </div>

          <div v-else-if="modalLoading" class="py-16 text-center text-sm text-slate-400">리뷰를 불러오는 중입니다.</div>
          <div v-else-if="modalError" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-12 text-center text-sm text-rose-700">{{ modalError }}</div>

          <form v-else-if="modalMode === 'edit' && activeReview" class="space-y-4" @submit.prevent="submitEdit">
            <div>
              <p class="mb-2 text-sm font-medium text-slate-700">평점</p>
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
              <textarea v-model="editForm.content" class="surface-soft min-h-32 w-full rounded-xl px-4 py-3 text-sm outline-none"></textarea>
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">이미지 URL (줄바꿈으로 구분)</span>
              <textarea v-model="editForm.imageUrlText" class="surface-soft min-h-24 w-full rounded-xl px-4 py-3 text-sm outline-none" placeholder="이미지 URL을 입력해 주세요"></textarea>
            </label>

            <p v-if="formMessage" class="rounded-xl px-4 py-3 text-sm" :class="formMessageClass">{{ formMessage }}</p>

            <div class="flex justify-end gap-3">
              <button type="button" class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50" @click="cancelEdit">
                취소
              </button>
              <button type="submit" class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400" :disabled="modalSubmitting">
                {{ modalSubmitting ? '저장 중...' : '리뷰 저장' }}
              </button>
            </div>
          </form>

          <template v-else-if="activeReview">
            <div class="mb-5">
              <div class="flex items-center gap-3">
                <span class="point-chip">{{ renderStars(activeReview.rating) }}</span>
                <span class="text-sm text-slate-500">{{ formatDate(activeReview.createdAt) }}</span>
              </div>
            </div>

            <div class="rounded-[1.5rem] bg-slate-50 px-5 py-5 text-sm leading-7 text-slate-700 whitespace-pre-line">
              {{ activeReview.content }}
            </div>

            <div v-if="activeReview.imageUrls?.length" class="mt-4 grid grid-cols-2 gap-3">
              <img
                v-for="(url, i) in activeReview.imageUrls"
                :key="i"
                :src="url"
                :alt="`리뷰 이미지 ${i + 1}`"
                class="aspect-square w-full rounded-2xl border border-slate-200 object-cover"
              >
            </div>

            <div class="mt-4 grid grid-cols-2 gap-3">
              <div class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-center text-sm font-semibold text-emerald-700">
                좋아요 {{ activeReview.likeCount ?? 0 }}
              </div>
              <div class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-center text-sm font-semibold text-rose-700">
                별로예요 {{ activeReview.dislikeCount ?? 0 }}
              </div>
            </div>

            <div class="mt-6 flex justify-end gap-3">
              <button
                class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                @click="startEdit"
              >
                수정
              </button>
              <button
                class="rounded-xl bg-rose-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:bg-slate-300"
                :disabled="modalSubmitting"
                @click="deleteCurrentReview"
              >
                {{ modalSubmitting ? '삭제 중...' : '삭제' }}
              </button>
            </div>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { reviewApi } from '@/api/board'

const router = useRouter()

const reviews = ref([])
const loading = ref(true)
const deleting = ref(false)
const checkedIds = ref(new Set())

const modalOpen = ref(false)
const modalMode = ref('detail')
const modalLoading = ref(false)
const modalSubmitting = ref(false)
const modalError = ref('')
const activeReview = ref(null)
const formMessage = ref('')
const formMessageClass = ref('bg-emerald-50 text-emerald-700')

const editForm = reactive({
  rating: 5,
  content: '',
  imageUrlText: '',
})

const isAllChecked = computed(() => reviews.value.length > 0 && checkedIds.value.size === reviews.value.length)
const isSomeChecked = computed(() => checkedIds.value.size > 0 && checkedIds.value.size < reviews.value.length)

function toggleAll(e) {
  checkedIds.value = e.target.checked
    ? new Set(reviews.value.map((r) => r.reviewId))
    : new Set()
}

function toggleItem(id) {
  const next = new Set(checkedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  checkedIds.value = next
}

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

function openWriteGuide() {
  modalOpen.value = true
  modalMode.value = 'guide'
  modalError.value = ''
}

async function openDetailModal(id) {
  modalOpen.value = true
  modalMode.value = 'detail'
  modalLoading.value = true
  modalError.value = ''
  formMessage.value = ''
  try {
    const { data } = await reviewApi.getMyDetail(id)
    activeReview.value = data
  } catch (e) {
    modalError.value = e.response?.data?.message || '리뷰를 불러오지 못했습니다.'
  } finally {
    modalLoading.value = false
  }
}

function closeModal() {
  modalOpen.value = false
  modalMode.value = 'detail'
  modalLoading.value = false
  modalSubmitting.value = false
  modalError.value = ''
  activeReview.value = null
  formMessage.value = ''
  formMessageClass.value = 'bg-emerald-50 text-emerald-700'
  editForm.rating = 5
  editForm.content = ''
  editForm.imageUrlText = ''
}

function startEdit() {
  if (!activeReview.value) return
  editForm.rating = activeReview.value.rating
  editForm.content = activeReview.value.content
  editForm.imageUrlText = (activeReview.value.imageUrls || []).join('\n')
  formMessage.value = ''
  modalMode.value = 'edit'
}

function cancelEdit() {
  modalMode.value = 'detail'
  formMessage.value = ''
}

async function submitEdit() {
  if (!editForm.content.trim()) {
    formMessage.value = '리뷰 내용을 입력해 주세요.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
    return
  }

  modalSubmitting.value = true
  try {
    await reviewApi.updateMy(activeReview.value.reviewId, {
      rating: editForm.rating,
      content: editForm.content.trim(),
      imageUrls: editForm.imageUrlText
        .split('\n')
        .map((v) => v.trim())
        .filter(Boolean),
    })
    await fetchMyReviews()
    await openDetailModal(activeReview.value.reviewId)
  } catch (e) {
    formMessage.value = e.response?.data?.message || '수정에 실패했습니다.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    modalSubmitting.value = false
  }
}

async function deleteCurrentReview() {
  if (!activeReview.value) return
  if (!confirm('이 리뷰를 삭제하시겠습니까?')) return

  modalSubmitting.value = true
  try {
    await reviewApi.removeMy(activeReview.value.reviewId)
    await fetchMyReviews()
    closeModal()
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  } finally {
    modalSubmitting.value = false
  }
}

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

function goOrderPage() {
  closeModal()
  router.push('/mypage/order')
}

function renderStars(rating) {
  return '★'.repeat(rating ?? 0) + '☆'.repeat(5 - (rating ?? 0))
}

function formatDate(v) {
  if (!v) return '-'
  return new Date(v).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(fetchMyReviews)
</script>
