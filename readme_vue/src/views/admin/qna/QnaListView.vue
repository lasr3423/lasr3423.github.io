<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="point-chip">관리자 QnA</p>
          <h1 class="section-title mt-3">문의 상태를 관리하고 관리자 답변을 등록하세요</h1>
          <p class="mt-2 text-sm text-slate-500">
            미답변 문의 확인과 답변 등록, 상태 변경을 처리할 수 있습니다.
          </p>
        </div>

        <label class="inline-flex items-center gap-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-700">
          <input v-model="unansweredOnly" type="checkbox" class="rounded" @change="search">
          미답변 문의만 보기
        </label>
      </div>
    </section>

    <section class="surface-panel overflow-hidden rounded-[2rem]">
      <div v-if="loading" class="p-12 text-center text-sm text-slate-500">문의 목록을 불러오는 중입니다...</div>

      <template v-else-if="qnas.length > 0">
        <div class="divide-y divide-slate-100">
          <article v-for="qna in qnas" :key="qna.qnaId" class="p-6">
            <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div class="flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="rounded-full px-3 py-1 text-xs font-semibold"
                        :class="qna.secret ? 'bg-amber-50 text-amber-700' : 'bg-slate-100 text-slate-600'">
                    {{ qna.secret ? '비밀글' : '공개글' }}
                  </span>
                  <span class="rounded-full px-3 py-1 text-xs font-semibold"
                        :class="qna.answer ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'">
                    {{ qna.answer ? '답변 완료' : '미답변' }}
                  </span>
                  <span class="text-xs font-medium text-slate-400">{{ qna.category || '일반 문의' }}</span>
                </div>

                <h2 class="mt-3 text-lg font-semibold text-slate-900">{{ qna.title }}</h2>
                <p class="mt-2 whitespace-pre-line text-sm leading-7 text-slate-600">{{ qna.content }}</p>
                <div class="mt-3 flex flex-wrap items-center gap-3 text-xs text-slate-400">
                  <span>작성자 {{ qna.memberName }}</span>
                  <span>{{ formatDateTime(qna.createdAt) }}</span>
                  <span>조회 {{ qna.viewCount }}</span>
                </div>
              </div>

              <div class="w-full max-w-xs rounded-2xl bg-slate-50 p-4">
                <label class="block space-y-2">
                  <span class="text-sm font-semibold text-slate-700">문의 상태</span>
                  <select
                    :value="qna.qnaStatus"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none"
                    @change="changeStatus(qna.qnaId, $event.target.value)"
                  >
                    <option value="WAITING">대기</option>
                    <option value="ANSWERED">답변 완료</option>
                    <option value="CLOSED">종료</option>
                  </select>
                </label>
              </div>
            </div>

            <div class="mt-5 rounded-2xl border border-slate-100 bg-slate-50 p-4">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="text-sm font-semibold text-slate-700">관리자 답변</p>
                  <p class="mt-1 text-xs text-slate-400">저장 시 바로 반영됩니다.</p>
                </div>
                <div v-if="qna.answer" class="flex gap-2">
                  <button class="rounded-xl border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700 transition hover:bg-white"
                          @click="startEditAnswer(qna)">수정</button>
                  <button class="rounded-xl bg-rose-500 px-3 py-2 text-xs font-semibold text-white transition hover:bg-rose-600"
                          @click="deleteAnswer(qna)">삭제</button>
                </div>
              </div>

              <div v-if="editingAnswerId === qna.qnaId || !qna.answer" class="mt-4 space-y-3">
                <textarea
                  v-model="answerInput[qna.qnaId]"
                  rows="4"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:ring-4 focus:ring-brand-100"
                  placeholder="관리자 답변을 입력해 주세요"
                />
                <div class="flex justify-end gap-2">
                  <button
                    v-if="editingAnswerId === qna.qnaId"
                    class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-white"
                    @click="cancelEditAnswer(qna)"
                  >
                    취소
                  </button>
                  <button
                    class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
                    @click="submitAnswer(qna)"
                  >
                    {{ qna.answer ? '답변 수정' : '답변 등록' }}
                  </button>
                </div>
              </div>
              <p v-else class="mt-4 whitespace-pre-line text-sm leading-7 text-slate-700">{{ qna.answer }}</p>
            </div>
          </article>
        </div>

        <div class="flex items-center justify-center gap-3 border-t border-slate-100 p-4">
          <button class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:opacity-40"
                  :disabled="page === 0" @click="movePage(page - 1)">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:opacity-40"
                  :disabled="page >= totalPages - 1" @click="movePage(page + 1)">다음</button>
        </div>
      </template>

      <div v-else class="p-12 text-center text-sm text-slate-500">등록된 문의가 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const qnas = ref([])
const loading = ref(true)
const unansweredOnly = ref(false)
const page = ref(0)
const totalPages = ref(1)
const answerInput = ref({})
const editingAnswerId = ref(null)

async function fetchQnas() {
  loading.value = true
  try {
    const { data } = await adminApi.getAdminQnas({ page: page.value, size: 20, unansweredOnly: unansweredOnly.value })
    qnas.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    for (const item of qnas.value) {
      if (!(item.qnaId in answerInput.value)) answerInput.value[item.qnaId] = item.answer || ''
    }
  } catch (error) {
    console.error('관리자 QnA 조회 실패', error)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 0
  fetchQnas()
}

function movePage(nextPage) {
  page.value = nextPage
  fetchQnas()
}

function findAnswerId(qna) {
  const child = qna.children?.find((item) => item.depth > qna.depth)
  return child?.qnaId
}

async function submitAnswer(qna) {
  const content = answerInput.value[qna.qnaId]?.trim()
  if (!content) {
    alert('답변 내용을 입력해 주세요.')
    return
  }

  try {
    if (qna.answer) await adminApi.updateAnswerQna(findAnswerId(qna), content)
    else await adminApi.answerQna(qna.qnaId, content)
    editingAnswerId.value = null
    await fetchQnas()
  } catch (error) {
    alert(error.response?.data?.message || '답변 저장 중 문제가 발생했습니다.')
  }
}

function startEditAnswer(qna) {
  editingAnswerId.value = qna.qnaId
  answerInput.value[qna.qnaId] = qna.answer || ''
}

function cancelEditAnswer(qna) {
  editingAnswerId.value = null
  answerInput.value[qna.qnaId] = qna.answer || ''
}

async function deleteAnswer(qna) {
  if (!confirm('관리자 답변을 삭제하시겠습니까?')) return
  try {
    await adminApi.deleteAnswerQna(findAnswerId(qna))
    editingAnswerId.value = null
    answerInput.value[qna.qnaId] = ''
    await fetchQnas()
  } catch (error) {
    alert(error.response?.data?.message || '답변 삭제 중 문제가 발생했습니다.')
  }
}

async function changeStatus(qnaId, status) {
  try {
    await adminApi.updateQnaStatus(qnaId, status)
    await fetchQnas()
  } catch (error) {
    alert(error.response?.data?.message || '문의 상태 변경 중 문제가 발생했습니다.')
  }
}

function formatDateTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(fetchQnas)
</script>
