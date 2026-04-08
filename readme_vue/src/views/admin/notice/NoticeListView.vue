<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">공지사항 관리</h1>
    </section>

    <!-- 등록 폼 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <h2 class="mb-4 text-sm font-bold text-slate-700">공지사항 등록</h2>
      <div class="space-y-3">
        <input v-model="form.title" type="text" placeholder="제목"
          class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
        <textarea v-model="form.content" rows="3" placeholder="내용"
          class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
        <div class="flex items-center justify-between">
          <label class="flex items-center gap-2 text-sm text-slate-600">
            <input v-model="form.pinned" type="checkbox" class="rounded" />
            상단 고정
          </label>
          <button @click="createNotice"
            class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-700">
            등록
          </button>
        </div>
      </div>
    </section>

    <!-- 목록 -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="notices.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">고정</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">제목</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">작성일</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="n in notices" :key="n.noticeId" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 text-center text-xs">
                  <span v-if="n.pinned" class="font-bold text-brand-800">📌</span>
                  <span v-else class="text-slate-300">—</span>
                </td>
                <td class="px-6 py-4 font-medium text-slate-800">{{ n.title }}</td>
                <td class="px-6 py-4 text-xs text-slate-400">{{ formatDate(n.createdAt) }}</td>
                <td class="px-6 py-4 text-center">
                  <span v-if="n.deletedAt" class="rounded-full bg-rose-50 px-3 py-1 text-xs font-semibold text-rose-600">삭제됨</span>
                  <span v-else class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">게시중</span>
                </td>
                <td class="px-6 py-4 text-right">
                  <button v-if="!n.deletedAt"
                    class="rounded-xl bg-rose-500 px-3 py-1.5 text-xs font-semibold text-white hover:bg-rose-600"
                    @click="deleteNotice(n.noticeId)">
                    삭제
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchNotices()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchNotices()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">공지사항이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const notices    = ref([])
const loading    = ref(true)
const page       = ref(0)
const totalPages = ref(1)
const form       = reactive({ title: '', content: '', pinned: false })

const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-'

async function fetchNotices() {
  loading.value = true
  try {
    const { data } = await adminApi.getAdminNotices({ page: page.value, size: 20 })
    notices.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('공지사항 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function createNotice() {
  if (!form.title.trim() || !form.content.trim()) return alert('제목과 내용을 입력해주세요.')
  try {
    await adminApi.createNotice({ title: form.title, content: form.content, pinned: form.pinned })
    Object.assign(form, { title: '', content: '', pinned: false })
    fetchNotices()
  } catch (e) {
    alert(e.response?.data?.message || '등록 실패')
  }
}

async function deleteNotice(id) {
  if (!confirm('이 공지사항을 삭제하시겠습니까?')) return
  try {
    await adminApi.deleteNotice(id)
    fetchNotices()
  } catch (e) {
    alert(e.response?.data?.message || '삭제 실패')
  }
}

onMounted(fetchNotices)
</script>
