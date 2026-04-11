<template>
  <div class="space-y-6">

    <!-- 헤더 -->
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="point-chip">내 문의 관리</p>
          <h1 class="section-title mt-3">내가 작성한 QnA</h1>
          <p class="mt-2 text-sm text-slate-500">배송, 결제, 상품 관련 문의를 확인하고 관리하세요.</p>
        </div>
        <button
          class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="showWriteForm = true"
        >
          + 새 문의 작성
        </button>
      </div>
    </section>

    <!-- 새 문의 작성 폼 (토글) -->
    <section v-if="showWriteForm" class="surface-panel rounded-[2rem] p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">New Question</p>
          <h2 class="mt-1 text-lg font-bold text-slate-900">문의 작성</h2>
        </div>
        <button class="text-sm text-slate-400 hover:text-slate-600" @click="closeWriteForm">✕ 닫기</button>
      </div>

      <form class="grid gap-4 md:grid-cols-2" @submit.prevent="submitCreate">
        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">문의 유형</span>
          <select v-model="createForm.category" class="surface-soft w-full rounded-xl px-4 py-3 text-sm outline-none">
            <option value="배송">배송</option>
            <option value="결제">결제</option>
            <option value="상품">상품</option>
            <option value="기타">기타</option>
          </select>
        </label>

        <label class="flex items-end gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
          <input v-model="createForm.isSecret" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-brand-700">
          <span class="text-sm text-slate-600">비밀글로 등록</span>
        </label>

        <label class="block space-y-2 md:col-span-2">
          <span class="text-sm font-medium text-slate-700">제목</span>
          <input v-model="createForm.title" class="surface-soft w-full rounded-xl px-4 py-3 text-sm outline-none" type="text" placeholder="문의 제목을 입력해 주세요">
        </label>

        <label class="block space-y-2 md:col-span-2">
          <span class="text-sm font-medium text-slate-700">내용</span>
          <textarea v-model="createForm.content" class="surface-soft min-h-28 w-full rounded-xl px-4 py-3 text-sm outline-none" placeholder="문의 내용을 자세히 적어 주세요"></textarea>
        </label>

        <p v-if="createMessage" class="md:col-span-2 rounded-xl px-4 py-3 text-sm" :class="createMessageClass">{{ createMessage }}</p>

        <div class="md:col-span-2 flex justify-end gap-3">
          <button type="button" class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50" @click="closeWriteForm">
            취소
          </button>
          <button type="submit" class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400" :disabled="creating">
            {{ creating ? '등록 중...' : '문의 등록' }}
          </button>
        </div>
      </form>
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
            {{ checkedIds.size > 0 ? `${checkedIds.size}건 선택됨` : `전체 ${qnas.length}건` }}
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
      <div v-else-if="qnas.length === 0" class="px-6 py-16 text-center">
        <p class="text-sm text-slate-500">작성한 문의가 없습니다.</p>
        <button class="mt-4 rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700" @click="showWriteForm = true">
          첫 문의 작성하기
        </button>
      </div>

      <!-- 문의 행 -->
      <ul v-else class="divide-y divide-slate-100">
        <li
          v-for="qna in qnas"
          :key="qna.qnaId"
          class="flex items-start gap-4 px-6 py-4 transition hover:bg-slate-50"
        >
          <!-- 체크박스 -->
          <input
            type="checkbox"
            class="mt-1 h-4 w-4 shrink-0 rounded border-slate-300 text-brand-700"
            :checked="checkedIds.has(qna.qnaId)"
            @change="toggleItem(qna.qnaId)"
            @click.stop
          >

          <!-- 클릭 영역 → 상세 이동 -->
          <div class="min-w-0 flex-1 cursor-pointer" @click="goDetail(qna.qnaId)">
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-semibold text-brand-700">{{ qna.category }}</span>
              <span class="rounded-full px-2.5 py-0.5 text-xs font-semibold" :class="statusClass(qna.qnaStatus)">{{ statusLabel(qna.qnaStatus) }}</span>
              <span v-if="qna.secret" class="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-500">비밀글</span>
            </div>
            <p class="mt-2 font-semibold text-slate-900 hover:text-brand-800 line-clamp-1">{{ qna.title }}</p>
            <p class="mt-1 line-clamp-1 text-sm text-slate-500">{{ qna.content }}</p>
            <p class="mt-1.5 text-xs text-slate-400">{{ formatDate(qna.createdAt) }}</p>
          </div>

          <!-- 상세 보기 화살표 -->
          <button
            class="shrink-0 rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-brand-200 hover:text-brand-700"
            @click="goDetail(qna.qnaId)"
          >
            상세 →
          </button>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { qnaApi } from '@/api/board'

const router = useRouter()

const qnas = ref([])
const loading = ref(true)
const deleting = ref(false)
const creating = ref(false)
const showWriteForm = ref(false)
const checkedIds = ref(new Set())

const createForm = reactive({ category: '배송', title: '', content: '', isSecret: false })
const createMessage = ref('')
const createMessageClass = ref('bg-emerald-50 text-emerald-700')

// ─── 전체/부분 선택 상태 ────────────────────────────────────────────────────
const isAllChecked = computed(() => qnas.value.length > 0 && checkedIds.value.size === qnas.value.length)
const isSomeChecked = computed(() => checkedIds.value.size > 0 && checkedIds.value.size < qnas.value.length)

function toggleAll(e) {
  if (e.target.checked) {
    checkedIds.value = new Set(qnas.value.map((q) => q.qnaId))
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
async function fetchMyQnas() {
  loading.value = true
  try {
    const { data } = await qnaApi.getMyList({ page: 0, size: 100, sort: 'createdAt,desc' })
    qnas.value = data.content ?? []
    checkedIds.value = new Set()
  } catch (e) {
    console.error('내 문의 목록 조회 실패', e)
    qnas.value = []
  } finally {
    loading.value = false
  }
}

// ─── 상세 이동 ─────────────────────────────────────────────────────────────
function goDetail(id) {
  router.push(`/mypage/qna/detail/${id}`)
}

// ─── 선택 삭제 ─────────────────────────────────────────────────────────────
async function deleteSelected() {
  if (checkedIds.value.size === 0) return
  if (!confirm(`선택한 ${checkedIds.value.size}건의 문의를 삭제하시겠습니까?\n(답변이 달린 문의는 삭제되지 않을 수 있습니다.)`)) return

  deleting.value = true
  const ids = [...checkedIds.value]
  const results = await Promise.allSettled(ids.map((id) => qnaApi.removeMy(id)))
  const failed = results.filter((r) => r.status === 'rejected').length

  if (failed > 0) {
    alert(`${ids.length - failed}건 삭제 완료, ${failed}건 실패 (답변이 달린 문의는 삭제할 수 없습니다.)`)
  }

  deleting.value = false
  await fetchMyQnas()
}

// ─── 문의 등록 ─────────────────────────────────────────────────────────────
async function submitCreate() {
  if (!createForm.title.trim() || !createForm.content.trim()) {
    createMessage.value = '제목과 내용을 모두 입력해 주세요.'
    createMessageClass.value = 'bg-rose-50 text-rose-700'
    return
  }
  creating.value = true
  try {
    const res = await qnaApi.create({
      category: createForm.category,
      title: createForm.title.trim(),
      content: createForm.content.trim(),
      isSecret: createForm.isSecret,
    })
    closeWriteForm()
    await fetchMyQnas()
    // 등록 직후 상세로 이동
    const newId = res.data?.qnaId ?? res.data?.id
    if (newId) router.push(`/mypage/qna/detail/${newId}`)
  } catch (e) {
    createMessage.value = e.response?.data?.message || '문의 등록에 실패했습니다.'
    createMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    creating.value = false
  }
}

function closeWriteForm() {
  showWriteForm.value = false
  Object.assign(createForm, { category: '배송', title: '', content: '', isSecret: false })
  createMessage.value = ''
}

// ─── 유틸 ──────────────────────────────────────────────────────────────────
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

onMounted(fetchMyQnas)
</script>
