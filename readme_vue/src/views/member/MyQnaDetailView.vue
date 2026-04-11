<template>
  <div class="space-y-6">

    <!-- 헤더 -->
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="point-chip">내 문의 상세</p>
          <h1 class="section-title mt-3">문의 상세 보기</h1>
        </div>
        <button
          class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
          @click="router.push('/mypage/qna')"
        >
          ← 목록으로
        </button>
      </div>
    </section>

    <!-- 로딩 -->
    <div v-if="loading" class="surface-panel rounded-[2rem] px-6 py-20 text-center text-sm text-slate-400">
      문의를 불러오는 중입니다.
    </div>

    <!-- 에러 -->
    <div v-else-if="error" class="surface-panel rounded-[2rem] px-6 py-20 text-center">
      <p class="text-sm text-rose-500">{{ error }}</p>
      <button class="mt-4 rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700" @click="router.push('/mypage/qna')">
        목록으로 돌아가기
      </button>
    </div>

    <!-- 본문 -->
    <template v-else-if="qna">

      <!-- 문의 내용 카드 -->
      <section class="surface-panel rounded-[2rem] p-6">
        <div class="mb-5 flex flex-wrap items-center gap-2">
          <span class="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-semibold text-brand-700">{{ qna.category }}</span>
          <span class="rounded-full px-2.5 py-0.5 text-xs font-semibold" :class="statusClass(qna.qnaStatus)">{{ statusLabel(qna.qnaStatus) }}</span>
          <span v-if="qna.secret" class="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-500">비밀글</span>
          <span class="ml-auto text-xs text-slate-400">{{ formatDate(qna.createdAt) }}</span>
        </div>

        <!-- 수정 폼 -->
        <form v-if="editing" class="space-y-4" @submit.prevent="submitEdit">
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">문의 유형</span>
            <select v-model="editForm.category" class="surface-soft w-full rounded-xl px-4 py-3 text-sm outline-none">
              <option value="배송">배송</option>
              <option value="결제">결제</option>
              <option value="상품">상품</option>
              <option value="기타">기타</option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">제목</span>
            <input v-model="editForm.title" class="surface-soft w-full rounded-xl px-4 py-3 text-sm outline-none" type="text">
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">내용</span>
            <textarea v-model="editForm.content" class="surface-soft min-h-32 w-full rounded-xl px-4 py-3 text-sm outline-none"></textarea>
          </label>

          <label class="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
            <input v-model="editForm.isSecret" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-brand-700">
            <span class="text-sm text-slate-600">비밀글로 등록</span>
          </label>

          <p v-if="editMessage" class="rounded-xl px-4 py-3 text-sm" :class="editMessageClass">{{ editMessage }}</p>

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
          <h2 class="text-xl font-bold text-slate-900">{{ qna.title }}</h2>
          <div class="mt-4 rounded-[1.5rem] bg-slate-50 px-5 py-5 text-sm leading-7 text-slate-700 whitespace-pre-line">
            {{ qna.content }}
          </div>

          <!-- 수정/삭제 (답변 없을 때만) -->
          <div v-if="!hasAnswer" class="mt-5 flex gap-3">
            <button
              class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              @click="startEdit"
            >
              문의 수정
            </button>
            <button
              class="rounded-xl bg-rose-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:bg-slate-300"
              :disabled="deleting"
              @click="deleteQna"
            >
              {{ deleting ? '삭제 중...' : '문의 삭제' }}
            </button>
          </div>
        </template>
      </section>

      <!-- 답변 섹션 -->
      <section class="surface-panel rounded-[2rem] p-6">
        <div class="mb-4">
          <p class="text-sm font-semibold text-brand-700">Answer</p>
          <h2 class="mt-1 text-lg font-bold text-slate-900">관리자 답변</h2>
        </div>

        <div v-if="!hasAnswer" class="rounded-[1.5rem] bg-slate-50 px-5 py-10 text-center text-sm text-slate-400">
          아직 답변이 등록되지 않았습니다.
        </div>

        <div v-else class="space-y-3">
          <div class="flex items-center justify-between">
            <span class="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">답변 완료</span>
            <span class="text-xs text-slate-400">{{ formatDate(qna.answeredAt) }}</span>
          </div>
          <div class="rounded-[1.5rem] border border-brand-100 bg-brand-50/40 px-5 py-5 text-sm leading-7 text-slate-700 whitespace-pre-line">
            {{ qna.answer }}
          </div>
        </div>
      </section>

    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { qnaApi } from '@/api/board'

const route = useRoute()
const router = useRouter()

const qna = ref(null)
const loading = ref(true)
const error = ref('')
const editing = ref(false)
const editSubmitting = ref(false)
const deleting = ref(false)
const editMessage = ref('')
const editMessageClass = ref('bg-emerald-50 text-emerald-700')

const editForm = reactive({ category: '배송', title: '', content: '', isSecret: false })

const hasAnswer = computed(() => !!qna.value?.answer)

// ─── 상세 조회 ─────────────────────────────────────────────────────────────
async function fetchDetail() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await qnaApi.getMyDetail(route.params.id)
    qna.value = data
  } catch (e) {
    error.value = e.response?.data?.message || '문의를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

// ─── 수정 ─────────────────────────────────────────────────────────────────
function startEdit() {
  editForm.category = qna.value.category
  editForm.title = qna.value.title
  editForm.content = qna.value.content
  editForm.isSecret = qna.value.secret ?? false
  editing.value = true
  editMessage.value = ''
}

function cancelEdit() {
  editing.value = false
  editMessage.value = ''
}

async function submitEdit() {
  if (!editForm.title.trim() || !editForm.content.trim()) {
    editMessage.value = '제목과 내용을 모두 입력해 주세요.'
    editMessageClass.value = 'bg-rose-50 text-rose-700'
    return
  }
  editSubmitting.value = true
  try {
    await qnaApi.updateMy(qna.value.qnaId, {
      category: editForm.category,
      title: editForm.title.trim(),
      content: editForm.content.trim(),
      isSecret: editForm.isSecret,
    })
    editing.value = false
    editMessage.value = ''
    await fetchDetail()
  } catch (e) {
    editMessage.value = e.response?.data?.message || '수정에 실패했습니다.'
    editMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    editSubmitting.value = false
  }
}

// ─── 삭제 ─────────────────────────────────────────────────────────────────
async function deleteQna() {
  if (!confirm('이 문의를 삭제하시겠습니까?')) return
  deleting.value = true
  try {
    await qnaApi.removeMy(qna.value.qnaId)
    router.push('/mypage/qna')
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
    deleting.value = false
  }
}

// ─── 유틸 ─────────────────────────────────────────────────────────────────
function statusLabel(s) {
  return { WAITING: '답변 대기', PROCESSING: '확인 중', COMPLETE: '답변 완료' }[s] ?? s
}
function statusClass(s) {
  return {
    WAITING: 'bg-amber-50 text-amber-700',
    PROCESSING: 'bg-sky-50 text-sky-700',
    COMPLETE: 'bg-emerald-50 text-emerald-700',
  }[s] ?? 'bg-slate-100 text-slate-600'
}
function formatDate(v) {
  if (!v) return '-'
  return new Date(v).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(fetchDetail)
</script>
