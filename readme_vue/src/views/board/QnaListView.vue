<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">Q&amp;A</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        배송, 결제, 상품 관련 문의를 등록하고 답변 상태를 확인하실 수 있습니다.
      </p>
    </header>

    <section class="surface-panel overflow-hidden">
      <div class="flex flex-col gap-4 border-b border-slate-100 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-semibold transition"
            :class="activeTab === 'public' ? 'bg-brand-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            @click="switchTab('public')"
          >
            공개 문의
          </button>
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50"
            :class="activeTab === 'my' ? 'bg-brand-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            :disabled="!authStore.isLoggedIn"
            @click="switchTab('my')"
          >
            내 문의
          </button>
        </div>

        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-500">{{ currentQnas.length }}건</span>
          <button
            type="button"
            class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="openWriteModal"
          >
            문의 작성
          </button>
        </div>
      </div>

      <div v-if="listLoading" class="px-6 py-16 text-center text-sm text-slate-400">Q&amp;A 목록을 불러오는 중입니다.</div>

      <div v-else-if="currentQnas.length === 0" class="px-6 py-16 text-center text-sm text-slate-400">
        {{ activeTab === 'public' ? '등록된 공개 문의가 없습니다.' : '작성한 문의가 없습니다.' }}
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[920px] table-fixed text-sm">
          <colgroup>
            <col class="w-16">
            <col class="w-28">
            <col>
            <col class="w-28">
            <col class="w-28">
            <col class="w-28">
          </colgroup>
          <thead class="border-b border-slate-200 bg-slate-50">
            <tr class="text-slate-500">
              <th class="px-4 py-3 text-center font-semibold">No</th>
              <th class="px-4 py-3 text-center font-semibold">카테고리</th>
              <th class="px-4 py-3 text-left font-semibold">제목</th>
              <th class="px-4 py-3 text-center font-semibold">작성자</th>
              <th class="px-4 py-3 text-center font-semibold">상태</th>
              <th class="px-4 py-3 text-center font-semibold">작성일</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="(qna, index) in currentQnas"
              :key="qna.qnaId"
              class="transition hover:bg-slate-50"
            >
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ currentQnas.length - index }}</td>
              <td class="px-4 py-4 text-center">
                <span class="inline-flex max-w-full truncate rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700">
                  {{ qna.category }}
                </span>
              </td>
              <td class="px-4 py-4">
                <button
                  type="button"
                  class="flex w-full min-w-0 items-center gap-2 text-left"
                  @click="openDetailModal(qna)"
                >
                  <span v-if="qna.secret" class="shrink-0 rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-semibold text-slate-500">비밀글</span>
                  <span class="card-title-1 block min-w-0 font-medium text-slate-900 hover:text-brand-800">{{ qna.title }}</span>
                </button>
              </td>
              <td class="px-4 py-4 text-center text-slate-500">{{ qna.memberName }}</td>
              <td class="px-4 py-4 text-center">
                <span class="inline-flex max-w-full truncate rounded-full px-2.5 py-1 text-xs font-semibold" :class="statusClass(qna.qnaStatus)">
                  {{ statusLabel(qna.qnaStatus) }}
                </span>
              </td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ formatDate(qna.createdAt) }}</td>
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
            <p class="point-chip">{{ modalMode === 'write' ? '문의 작성' : modalMode === 'edit' ? '문의 수정' : '문의 상세' }}</p>
            <h2 class="mt-3 text-xl font-bold text-slate-900">
              {{ modalMode === 'write' ? '새 문의 작성' : modalMode === 'edit' ? '문의 수정' : selectedQna?.title || '문의 상세' }}
            </h2>
          </div>
          <button
            type="button"
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
            @click="closeModal"
          >
            닫기
          </button>
        </header>

        <div class="max-h-[calc(90vh-104px)] overflow-y-auto px-6 py-6">
          <div v-if="detailLoading" class="py-16 text-center text-sm text-slate-400">문의 상세를 불러오는 중입니다.</div>

          <form v-else-if="modalMode === 'write' || modalMode === 'edit'" class="space-y-4" @submit.prevent="submitQna">
            <div class="grid gap-4 md:grid-cols-2">
              <label class="block space-y-2">
                <span class="text-sm font-medium text-slate-700">문의 유형</span>
                <select v-model="form.category" class="surface-soft w-full px-4 py-3 text-sm outline-none">
                  <option value="배송">배송</option>
                  <option value="결제">결제</option>
                  <option value="상품">상품</option>
                  <option value="기타">기타</option>
                </select>
              </label>

              <label class="flex items-end gap-3 rounded-[1.5rem] border border-slate-200 bg-slate-50 px-4 py-3">
                <input v-model="form.isSecret" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-brand-700">
                <span class="text-sm text-slate-600">비밀글로 등록</span>
              </label>
            </div>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">제목</span>
              <input v-model="form.title" class="surface-soft w-full px-4 py-3 text-sm outline-none" type="text" placeholder="문의 제목을 입력해 주세요">
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">내용</span>
              <textarea v-model="form.content" class="surface-soft min-h-32 w-full px-4 py-3 text-sm outline-none" placeholder="문의 내용을 자세히 적어 주세요"></textarea>
            </label>

            <p v-if="formMessage" class="rounded-2xl px-4 py-3 text-sm" :class="formMessageClass">
              {{ formMessage }}
            </p>

            <div class="flex justify-end gap-3">
              <button
                type="button"
                class="rounded-full border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                @click="closeModal"
              >
                취소
              </button>
              <button
                type="submit"
                class="rounded-full bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                :disabled="!authStore.isLoggedIn || submitting"
              >
                {{ submitting ? (editingQnaId ? '수정 중...' : '등록 중...') : (editingQnaId ? '문의 수정' : '문의 등록') }}
              </button>
            </div>
          </form>

          <template v-else-if="selectedQna">
            <div class="flex flex-wrap items-center gap-2">
              <span class="point-chip">{{ selectedQna.category }}</span>
              <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(selectedQna.qnaStatus)">
                {{ statusLabel(selectedQna.qnaStatus) }}
              </span>
              <span v-if="selectedQna.secret" class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">비밀글</span>
              <span class="ml-auto text-sm text-slate-400">{{ formatDate(selectedQna.createdAt) }}</span>
            </div>

            <dl class="mt-5 grid gap-3 sm:grid-cols-3">
              <div class="rounded-2xl bg-slate-50 p-4">
                <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</dt>
                <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedQna.memberName }}</dd>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">상태</dt>
                <dd class="mt-2 text-sm font-semibold text-slate-900">{{ statusLabel(selectedQna.qnaStatus) }}</dd>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">조회수</dt>
                <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedQna.viewCount }}</dd>
              </div>
            </dl>

            <div class="mt-6 rounded-[1.5rem] bg-slate-50 px-5 py-6 text-sm leading-7 text-slate-700 whitespace-pre-line">
              {{ selectedQna.content }}
            </div>

            <div v-if="selectedAnswer" class="mt-6 rounded-[1.5rem] border border-brand-100 bg-brand-50/70 px-5 py-6">
              <p class="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Answer</p>
              <p class="mt-3 text-sm leading-7 text-slate-700">{{ selectedAnswer.content }}</p>
              <p class="mt-3 text-xs text-slate-500">답변일 {{ formatDate(selectedAnswer.createdAt) }}</p>
            </div>

            <div v-if="canManageSelectedQna" class="mt-6 flex justify-end gap-3">
              <button type="button" class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50" @click="startEditSelectedQna">
                문의 수정
              </button>
              <button type="button" class="rounded-2xl bg-rose-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-rose-600" @click="deleteSelectedQna">
                문의 삭제
              </button>
            </div>
          </template>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { qnaApi } from '@/api/board'
import { memberApi } from '@/api/member'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const activeTab = ref('public')
const publicQnas = ref([])
const myQnas = ref([])
const selectedQna = ref(null)
const currentMemberId = ref(null)
const listLoading = ref(true)
const detailLoading = ref(false)
const submitting = ref(false)
const editingQnaId = ref(null)
const modalOpen = ref(false)
const modalMode = ref('detail')
const formMessage = ref('')
const formMessageClass = ref('bg-emerald-50 text-emerald-700')

const form = reactive({
  category: '배송',
  title: '',
  content: '',
  isSecret: false,
})

const currentQnas = computed(() => (activeTab.value === 'public' ? publicQnas.value : myQnas.value))
const selectedAnswer = computed(() => selectedQna.value?.children?.find((child) => child.depth > selectedQna.value.depth) || null)
const canManageSelectedQna = computed(() => {
  if (!selectedQna.value || !currentMemberId.value) return false
  return selectedQna.value.memberId === currentMemberId.value && !selectedAnswer.value
})

async function initialize() {
  if (authStore.isLoggedIn) {
    try {
      const { data } = await memberApi.getMe()
      currentMemberId.value = data.id
    } catch (error) {
      console.error('회원 정보 조회 실패', error)
    }
  }
  await Promise.all([fetchPublicQnas(), fetchMyQnas()])
}

async function fetchPublicQnas() {
  listLoading.value = true
  try {
    const { data } = await qnaApi.getList({ page: 0, size: 20, sort: 'createdAt,desc' })
    publicQnas.value = data.content ?? []
  } catch (error) {
    console.error('공개 QnA 목록 조회 실패', error)
    publicQnas.value = []
  } finally {
    listLoading.value = false
  }
}

async function fetchMyQnas() {
  if (!authStore.isLoggedIn) {
    myQnas.value = []
    return
  }
  try {
    const { data } = await qnaApi.getMyList({ page: 0, size: 20, sort: 'createdAt,desc' })
    myQnas.value = data.content ?? []
  } catch (error) {
    console.error('내 문의 목록 조회 실패', error)
    myQnas.value = []
  }
}

async function switchTab(tab) {
  if (tab === 'my' && !authStore.isLoggedIn) return
  activeTab.value = tab
}

function resetForm() {
  editingQnaId.value = null
  form.category = '배송'
  form.title = ''
  form.content = ''
  form.isSecret = false
  formMessage.value = ''
  formMessageClass.value = 'bg-emerald-50 text-emerald-700'
}

function openWriteModal() {
  if (!authStore.isLoggedIn) {
    formMessage.value = '로그인 후 문의를 등록할 수 있습니다.'
    formMessageClass.value = 'bg-amber-50 text-amber-700'
    modalMode.value = 'write'
    modalOpen.value = true
    return
  }
  resetForm()
  modalMode.value = 'write'
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  detailLoading.value = false
  if (modalMode.value !== 'detail') {
    resetForm()
  }
  modalMode.value = 'detail'
}

async function openQnaDetail(qna) {
  detailLoading.value = true
  try {
    const fetcher = activeTab.value === 'my' || qna.secret ? qnaApi.getMyDetail : qnaApi.getDetail
    const { data } = await fetcher(qna.qnaId)
    selectedQna.value = data
    modalMode.value = 'detail'
    modalOpen.value = true
  } catch (error) {
    console.error('QnA 상세 조회 실패', error)
    formMessage.value = error.response?.data?.message || '문의 상세를 불러오지 못했습니다.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    detailLoading.value = false
  }
}

function openDetailModal(qna) {
  openQnaDetail(qna)
}

async function submitQna() {
  if (!authStore.isLoggedIn) {
    formMessage.value = '로그인 후 문의를 등록할 수 있습니다.'
    formMessageClass.value = 'bg-amber-50 text-amber-700'
    return
  }
  if (!form.title.trim() || !form.content.trim()) {
    formMessage.value = '제목과 내용을 모두 입력해 주세요.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
    return
  }

  submitting.value = true
  try {
    if (editingQnaId.value) {
      await qnaApi.updateMy(editingQnaId.value, {
        category: form.category,
        title: form.title.trim(),
        content: form.content.trim(),
        isSecret: form.isSecret,
      })
      formMessage.value = '문의가 수정되었습니다.'
    } else {
      await qnaApi.create({
        category: form.category,
        title: form.title.trim(),
        content: form.content.trim(),
        isSecret: form.isSecret,
      })
      formMessage.value = '문의가 등록되었습니다.'
    }
    formMessageClass.value = 'bg-emerald-50 text-emerald-700'
    await Promise.all([fetchPublicQnas(), fetchMyQnas()])
    closeModal()
  } catch (error) {
    console.error('QnA 저장 실패', error)
    formMessage.value = error.response?.data?.message || '문의 저장에 실패했습니다.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    submitting.value = false
  }
}

function startEditSelectedQna() {
  if (!selectedQna.value) return
  editingQnaId.value = selectedQna.value.qnaId
  form.category = selectedQna.value.category || '배송'
  form.title = selectedQna.value.title
  form.content = selectedQna.value.content
  form.isSecret = !!selectedQna.value.secret
  formMessage.value = ''
  modalMode.value = 'edit'
}

async function deleteSelectedQna() {
  if (!selectedQna.value || !confirm('선택한 문의를 삭제하시겠습니까?')) return
  try {
    await qnaApi.removeMy(selectedQna.value.qnaId)
    await Promise.all([fetchPublicQnas(), fetchMyQnas()])
    closeModal()
  } catch (error) {
    formMessage.value = error.response?.data?.message || '문의 삭제에 실패했습니다.'
    formMessageClass.value = 'bg-rose-50 text-rose-700'
  }
}

function statusLabel(status) {
  return ({ WAITING: '답변 대기', PROCESSING: '확인 중', COMPLETE: '답변 완료' }[status] ?? status)
}

function statusClass(status) {
  return ({
    WAITING: 'bg-amber-50 text-amber-700',
    PROCESSING: 'bg-sky-50 text-sky-700',
    COMPLETE: 'bg-emerald-50 text-emerald-700',
  }[status] ?? 'bg-slate-100 text-slate-600')
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

onMounted(initialize)
</script>
