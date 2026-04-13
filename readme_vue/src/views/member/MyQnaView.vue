<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="point-chip">내 문의 관리</p>
          <h1 class="section-title mt-3">내 문의</h1>
          <p class="mt-2 text-sm text-slate-500">배송, 결제, 상품 관련 문의를 확인하고 관리하실 수 있습니다.</p>
        </div>
        <button
          class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="openCreateModal"
        >
          문의 작성
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
            {{ checkedIds.size > 0 ? `${checkedIds.size}건 선택됨` : `전체 ${qnas.length}건` }}
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
            class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="openCreateModal"
          >
            문의 작성
          </button>
        </div>
      </div>

      <div v-if="loading" class="px-6 py-16 text-center text-sm text-slate-400">목록을 불러오는 중입니다.</div>

      <div v-else-if="qnas.length === 0" class="px-6 py-16 text-center">
        <p class="text-sm text-slate-500">작성한 문의가 없습니다.</p>
        <button class="mt-4 rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700" @click="openCreateModal">
          첫 문의 작성
        </button>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[760px] table-fixed text-sm">
          <colgroup>
            <col class="w-14">
            <col class="w-16">
            <col class="w-28">
            <col class="w-28">
            <col>
            <col class="w-28">
            <col class="w-24">
          </colgroup>
          <thead class="border-b border-slate-200 bg-slate-50">
            <tr class="text-slate-500">
              <th class="px-4 py-3 text-center font-semibold">선택</th>
              <th class="px-4 py-3 text-center font-semibold">No</th>
              <th class="px-4 py-3 text-center font-semibold">유형</th>
              <th class="px-4 py-3 text-center font-semibold">상태</th>
              <th class="px-4 py-3 text-left font-semibold">제목</th>
              <th class="px-4 py-3 text-center font-semibold">작성일</th>
              <th class="px-4 py-3 text-center font-semibold">보기</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="(qna, index) in qnas"
              :key="qna.qnaId"
              class="transition hover:bg-slate-50"
            >
              <td class="px-4 py-4 text-center">
                <input
                  type="checkbox"
                  class="h-4 w-4 rounded border-slate-300 text-brand-700"
                  :checked="checkedIds.has(qna.qnaId)"
                  @change="toggleItem(qna.qnaId)"
                >
              </td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ qnas.length - index }}</td>
              <td class="px-4 py-4 text-center">
                <span class="inline-flex max-w-full truncate rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700">
                  {{ qna.category }}
                </span>
              </td>
              <td class="px-4 py-4 text-center">
                <span class="inline-flex max-w-full truncate rounded-full px-2.5 py-1 text-xs font-semibold" :class="statusClass(qna.qnaStatus)">
                  {{ statusLabel(qna.qnaStatus) }}
                </span>
              </td>
              <td class="px-4 py-4">
                <button
                  type="button"
                  class="flex w-full min-w-0 items-center gap-2 text-left"
                  @click="openDetailModal(qna.qnaId)"
                >
                  <span v-if="qna.secret" class="shrink-0 rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-semibold text-slate-500">비밀글</span>
                  <span class="card-title-1 block min-w-0 font-medium text-slate-900 hover:text-brand-800">{{ qna.title }}</span>
                </button>
              </td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ formatDate(qna.createdAt) }}</td>
              <td class="px-4 py-4 text-center">
                <button
                  type="button"
                  class="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-brand-200 hover:text-brand-700"
                  @click="openDetailModal(qna.qnaId)"
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
            <p class="point-chip">{{ modalMode === 'create' ? '문의 작성' : modalMode === 'edit' ? '문의 수정' : '문의 상세' }}</p>
            <h2 class="mt-3 text-xl font-bold text-slate-900">
              {{ modalMode === 'create' ? '새 문의 작성' : modalMode === 'edit' ? '문의 수정' : activeQna?.title || '문의 상세' }}
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
          <div v-if="modalLoading" class="py-16 text-center text-sm text-slate-400">문의를 불러오는 중입니다.</div>
          <div v-else-if="modalError" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-12 text-center text-sm text-rose-700">{{ modalError }}</div>

          <form
            v-else-if="modalMode === 'create' || modalMode === 'edit'"
            class="space-y-4"
            @submit.prevent="submitModal"
          >
            <div class="grid gap-4 md:grid-cols-2">
              <label class="block space-y-2">
                <span class="text-sm font-medium text-slate-700">문의 유형</span>
                <select v-model="modalForm.category" class="surface-soft w-full rounded-xl px-4 py-3 text-sm outline-none">
                  <option value="배송">배송</option>
                  <option value="결제">결제</option>
                  <option value="상품">상품</option>
                  <option value="기타">기타</option>
                </select>
              </label>

              <label class="flex items-end gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
                <input v-model="modalForm.isSecret" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-brand-700">
                <span class="text-sm text-slate-600">비밀글로 등록</span>
              </label>
            </div>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">제목</span>
              <input v-model="modalForm.title" class="surface-soft w-full rounded-xl px-4 py-3 text-sm outline-none" type="text" placeholder="문의 제목을 입력해 주세요">
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">내용</span>
              <textarea v-model="modalForm.content" class="surface-soft min-h-32 w-full rounded-xl px-4 py-3 text-sm outline-none" placeholder="문의 내용을 입력해 주세요"></textarea>
            </label>

            <p v-if="modalMessage" class="rounded-xl px-4 py-3 text-sm" :class="modalMessageClass">{{ modalMessage }}</p>

            <div class="flex justify-end gap-3">
              <button type="button" class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50" @click="closeModal">
                취소
              </button>
              <button type="submit" class="rounded-xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400" :disabled="modalSubmitting">
                {{ modalSubmitting ? '저장 중...' : modalMode === 'create' ? '문의 등록' : '문의 저장' }}
              </button>
            </div>
          </form>

          <template v-else-if="activeQna">
            <div class="mb-5 flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-semibold text-brand-700">{{ activeQna.category }}</span>
              <span class="rounded-full px-2.5 py-0.5 text-xs font-semibold" :class="statusClass(activeQna.qnaStatus)">{{ statusLabel(activeQna.qnaStatus) }}</span>
              <span v-if="activeQna.secret" class="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-500">비밀글</span>
              <span class="ml-auto text-xs text-slate-400">{{ formatDate(activeQna.createdAt) }}</span>
            </div>

            <div class="rounded-[1.5rem] bg-slate-50 px-5 py-5 text-sm leading-7 text-slate-700 whitespace-pre-line">
              {{ activeQna.content }}
            </div>

            <section class="mt-5">
              <p class="text-sm font-semibold text-brand-700">관리자 답변</p>
              <div v-if="!hasAnswer" class="mt-3 rounded-[1.5rem] bg-slate-50 px-5 py-10 text-center text-sm text-slate-400">
                아직 답변이 등록되지 않았습니다.
              </div>
              <div v-else class="mt-3 space-y-3">
                <div class="flex items-center justify-between">
                  <span class="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">답변 완료</span>
                  <span class="text-xs text-slate-400">{{ formatDate(activeQna.answeredAt) }}</span>
                </div>
                <div class="rounded-[1.5rem] border border-brand-100 bg-brand-50/40 px-5 py-5 text-sm leading-7 text-slate-700 whitespace-pre-line">
                  {{ activeQna.answer }}
                </div>
              </div>
            </section>

            <div v-if="!hasAnswer" class="mt-6 flex justify-end gap-3">
              <button
                class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                @click="startEdit"
              >
                수정
              </button>
              <button
                class="rounded-xl bg-rose-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:bg-slate-300"
                :disabled="modalSubmitting"
                @click="deleteCurrentQna"
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
import { qnaApi } from '@/api/board'

const qnas = ref([])
const loading = ref(true)
const deleting = ref(false)
const checkedIds = ref(new Set())

const modalOpen = ref(false)
const modalMode = ref('detail')
const modalLoading = ref(false)
const modalSubmitting = ref(false)
const modalError = ref('')
const modalMessage = ref('')
const modalMessageClass = ref('bg-emerald-50 text-emerald-700')
const activeQna = ref(null)

const modalForm = reactive({
  category: '배송',
  title: '',
  content: '',
  isSecret: false,
})

const isAllChecked = computed(() => qnas.value.length > 0 && checkedIds.value.size === qnas.value.length)
const isSomeChecked = computed(() => checkedIds.value.size > 0 && checkedIds.value.size < qnas.value.length)
const hasAnswer = computed(() => !!activeQna.value?.answer)

function resetModalForm() {
  modalForm.category = '배송'
  modalForm.title = ''
  modalForm.content = ''
  modalForm.isSecret = false
}

function resetModalState() {
  modalLoading.value = false
  modalSubmitting.value = false
  modalError.value = ''
  modalMessage.value = ''
  modalMessageClass.value = 'bg-emerald-50 text-emerald-700'
}

function openCreateModal() {
  resetModalState()
  resetModalForm()
  activeQna.value = null
  modalMode.value = 'create'
  modalOpen.value = true
}

async function openDetailModal(id) {
  modalOpen.value = true
  modalMode.value = 'detail'
  modalLoading.value = true
  modalError.value = ''
  modalMessage.value = ''
  try {
    const { data } = await qnaApi.getMyDetail(id)
    activeQna.value = data
  } catch (e) {
    modalError.value = e.response?.data?.message || '문의를 불러오지 못했습니다.'
  } finally {
    modalLoading.value = false
  }
}

function closeModal() {
  modalOpen.value = false
  activeQna.value = null
  resetModalState()
  resetModalForm()
  modalMode.value = 'detail'
}

function startEdit() {
  if (!activeQna.value) return
  modalForm.category = activeQna.value.category
  modalForm.title = activeQna.value.title
  modalForm.content = activeQna.value.content
  modalForm.isSecret = activeQna.value.secret ?? false
  modalMessage.value = ''
  modalMode.value = 'edit'
}

function toggleAll(e) {
  checkedIds.value = e.target.checked
    ? new Set(qnas.value.map((q) => q.qnaId))
    : new Set()
}

function toggleItem(id) {
  const next = new Set(checkedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  checkedIds.value = next
}

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

async function submitModal() {
  if (!modalForm.title.trim() || !modalForm.content.trim()) {
    modalMessage.value = '제목과 내용을 모두 입력해 주세요.'
    modalMessageClass.value = 'bg-rose-50 text-rose-700'
    return
  }

  modalSubmitting.value = true
  try {
    const payload = {
      category: modalForm.category,
      title: modalForm.title.trim(),
      content: modalForm.content.trim(),
      isSecret: modalForm.isSecret,
    }

    if (modalMode.value === 'create') {
      const res = await qnaApi.create(payload)
      const newId = res.data?.qnaId ?? res.data?.id
      await fetchMyQnas()
      if (newId) {
        await openDetailModal(newId)
      } else {
        closeModal()
      }
      return
    }

    await qnaApi.updateMy(activeQna.value.qnaId, payload)
    await fetchMyQnas()
    await openDetailModal(activeQna.value.qnaId)
  } catch (e) {
    modalMessage.value = e.response?.data?.message || '저장에 실패했습니다.'
    modalMessageClass.value = 'bg-rose-50 text-rose-700'
  } finally {
    modalSubmitting.value = false
  }
}

async function deleteCurrentQna() {
  if (!activeQna.value) return
  if (!confirm('이 문의를 삭제하시겠습니까?')) return

  modalSubmitting.value = true
  try {
    await qnaApi.removeMy(activeQna.value.qnaId)
    await fetchMyQnas()
    closeModal()
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  } finally {
    modalSubmitting.value = false
  }
}

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
