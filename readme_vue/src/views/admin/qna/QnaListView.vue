<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">QnA 관리</h1>
    </section>

    <!-- 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <label class="flex items-center gap-2 text-sm text-slate-600">
          <input v-model="unansweredOnly" type="checkbox" class="rounded" @change="search" />
          미답변만 보기
        </label>
      </div>
    </section>

    <!-- 목록 -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="qnas.length > 0">
        <div class="divide-y divide-slate-100">
          <div v-for="q in qnas" :key="q.qnaId" class="p-6">
            <!-- 질문 -->
            <div class="flex items-start justify-between gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span v-if="q.secret" class="rounded-full bg-amber-50 px-2 py-0.5 text-xs font-semibold text-amber-600">🔒 비밀</span>
                  <p class="font-semibold text-slate-800">{{ q.title }}</p>
                </div>
                <p class="mt-1 line-clamp-2 text-sm text-slate-500">{{ q.content }}</p>
                <p class="mt-1 text-xs text-slate-400">{{ formatDate(q.createdAt) }}</p>
              </div>
              <span v-if="!q.answer" class="shrink-0 rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-600">미답변</span>
              <span v-else class="shrink-0 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">답변완료</span>
            </div>

            <!-- 기존 답변 -->
            <div v-if="q.answer" class="mt-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
              <p class="mb-1 text-xs font-semibold text-slate-400">관리자 답변</p>
              {{ q.answer }}
            </div>

            <!-- 답변 입력 -->
            <div v-if="!q.answer" class="mt-3 flex gap-2">
              <input
                v-model="answerInput[q.qnaId]"
                type="text" placeholder="답변을 입력하세요"
                class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none focus:border-brand-400 focus:bg-white"
              />
              <button
                class="shrink-0 rounded-2xl bg-brand-800 px-4 py-2 text-xs font-semibold text-white hover:bg-brand-700"
                @click="submitAnswer(q.qnaId)"
              >
                답변 등록
              </button>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchQnas()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchQnas()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">QnA가 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const qnas           = ref([])
const loading        = ref(true)
const unansweredOnly = ref(false)
const page           = ref(0)
const totalPages     = ref(1)
const answerInput    = ref({})

const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-'

async function fetchQnas() {
  loading.value = true
  try {
    const { data } = await adminApi.getAdminQnas({ page: page.value, size: 20, unansweredOnly: unansweredOnly.value })
    qnas.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('QnA 로드 실패', e)
  } finally {
    loading.value = false
  }
}

function search() { page.value = 0; fetchQnas() }

async function submitAnswer(qnaId) {
  const answer = answerInput.value[qnaId]?.trim()
  if (!answer) return alert('답변 내용을 입력해주세요.')
  try {
    await adminApi.answerQna(qnaId, answer)
    answerInput.value[qnaId] = ''
    fetchQnas()
  } catch (e) {
    alert(e.response?.data?.message || '답변 등록 실패')
  }
}

onMounted(fetchQnas)
</script>
