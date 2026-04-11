<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="point-chip">관리자 공지사항</p>
          <h1 class="section-title mt-3">공지사항 목록을 등록하고 정리해요</h1>
          <p class="mt-2 text-sm text-slate-500">
            고정 공지는 상단에 하이라이트로 노출되고, 선택한 공지사항은 한 번에 삭제할 수 있습니다.
          </p>
        </div>
        <div class="flex flex-wrap gap-3">
          <button
            class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="selectedIds.length === 0"
            @click="removeSelected"
          >
            삭제하기
          </button>
          <button
            class="rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="openCreate"
          >
            공지 등록
          </button>
        </div>
      </div>
    </section>

    <section class="surface-panel overflow-hidden rounded-[2rem]">
      <div v-if="loading" class="p-12 text-center text-sm text-slate-500">공지사항을 불러오는 중입니다...</div>

      <template v-else-if="sortedNotices.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-slate-50 text-slate-500">
              <tr>
                <th class="px-6 py-4 text-center font-semibold">
                  <input
                    type="checkbox"
                    :checked="allChecked"
                    class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400"
                    @change="toggleAll"
                  >
                </th>
                <th class="px-6 py-4 text-left font-semibold">제목</th>
                <th class="px-6 py-4 text-left font-semibold">작성자</th>
                <th class="px-6 py-4 text-center font-semibold">상태</th>
                <th class="px-6 py-4 text-right font-semibold">작성일</th>
                <th class="px-6 py-4 text-right font-semibold">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="notice in sortedNotices"
                :key="notice.noticeId"
                :class="notice.pinned ? 'bg-brand-50/40' : ''"
              >
                <td class="px-6 py-4 text-center">
                  <input
                    type="checkbox"
                    :checked="selectedIds.includes(notice.noticeId)"
                    class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400"
                    @change="toggleOne(notice.noticeId)"
                  >
                </td>
                <td class="px-6 py-4">
                  <RouterLink
                    :to="`/notice/${notice.noticeId}`"
                    class="block rounded-2xl px-1 py-1 transition hover:bg-white/70"
                  >
                    <div class="flex items-center gap-2">
                      <span
                        v-if="notice.pinned"
                        class="inline-flex rounded-full bg-brand-100 px-2.5 py-1 text-[11px] font-semibold text-brand-700"
                      >
                        고정
                      </span>
                      <p class="font-semibold text-slate-900">{{ notice.title }}</p>
                    </div>
                    <p class="mt-1 line-clamp-2 text-xs text-slate-500">{{ notice.content }}</p>
                  </RouterLink>
                </td>
                <td class="px-6 py-4 text-slate-600">{{ notice.authorName }}</td>
                <td class="px-6 py-4 text-center">
                  <span
                    class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                    :class="notice.deletedAt ? 'bg-rose-50 text-rose-600' : 'bg-emerald-50 text-emerald-700'"
                  >
                    {{ notice.deletedAt ? '삭제됨' : '게시중' }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right text-slate-500">{{ formatDate(notice.createdAt) }}</td>
                <td class="px-6 py-4">
                  <div class="flex justify-end gap-2">
                    <button
                      class="rounded-xl border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
                      @click="startEdit(notice)"
                    >
                      수정
                    </button>
                    <button
                      v-if="!notice.deletedAt"
                      class="rounded-xl bg-rose-500 px-3 py-2 text-xs font-semibold text-white transition hover:bg-rose-600"
                      @click="removeNotice(notice.noticeId)"
                    >
                      삭제
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-3 border-t border-slate-100 p-4">
          <button
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:opacity-40"
            :disabled="page === 0"
            @click="movePage(page - 1)"
          >
            이전
          </button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:opacity-40"
            :disabled="page >= totalPages - 1"
            @click="movePage(page + 1)"
          >
            다음
          </button>
        </div>
      </template>

      <div v-else class="p-12 text-center text-sm text-slate-500">등록된 공지사항이 없습니다.</div>
    </section>

    <div v-if="editorOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4">
      <div class="w-full max-w-2xl rounded-[2rem] bg-white p-6 shadow-2xl">
        <div class="flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-semibold text-slate-500">{{ editorMode === 'create' ? '공지 등록' : '공지 수정' }}</p>
            <h2 class="mt-1 text-2xl font-bold text-slate-900">
              {{ editorMode === 'create' ? '새 공지사항 작성' : '공지사항 수정' }}
            </h2>
          </div>
          <button class="text-sm font-medium text-slate-500 hover:text-slate-800" @click="closeEditor">닫기</button>
        </div>

        <div class="mt-6 space-y-4">
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">제목</span>
            <input
              v-model="form.title"
              type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
              placeholder="공지 제목을 입력해 주세요"
            >
          </label>
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">내용</span>
            <textarea
              v-model="form.content"
              rows="8"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
              placeholder="공지 내용을 입력해 주세요"
            />
          </label>
          <label class="flex items-center gap-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-700">
            <input v-model="form.pinned" type="checkbox" class="rounded" />
            상단 고정 공지로 노출
          </label>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button
            class="rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="closeEditor"
          >
            취소
          </button>
          <button
            class="rounded-xl bg-brand-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="saveNotice"
          >
            {{ editorMode === 'create' ? '등록하기' : '수정 저장' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { adminApi } from '@/api/admin'

const notices = ref([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(1)
const selectedIds = ref([])
const editorOpen = ref(false)
const editorMode = ref('create')
const editingId = ref(null)
const form = reactive({ title: '', content: '', pinned: false })

const sortedNotices = computed(() => {
  return [...notices.value].sort((a, b) => {
    if (a.pinned !== b.pinned) return a.pinned ? -1 : 1
    return new Date(b.createdAt) - new Date(a.createdAt)
  })
})

const allChecked = computed(() => {
  if (!sortedNotices.value.length) return false
  return sortedNotices.value.every((notice) => selectedIds.value.includes(notice.noticeId))
})

async function fetchNotices() {
  loading.value = true
  try {
    const { data } = await adminApi.getAdminNotices({ page: page.value, size: 20 })
    notices.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    selectedIds.value = selectedIds.value.filter((id) => notices.value.some((notice) => notice.noticeId === id))
  } catch (error) {
    console.error('공지사항 조회 실패', error)
  } finally {
    loading.value = false
  }
}

function movePage(nextPage) {
  page.value = nextPage
  fetchNotices()
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('ko-KR')
}

function toggleOne(noticeId) {
  if (selectedIds.value.includes(noticeId)) {
    selectedIds.value = selectedIds.value.filter((id) => id !== noticeId)
  } else {
    selectedIds.value = [...selectedIds.value, noticeId]
  }
}

function toggleAll(event) {
  if (event.target.checked) {
    selectedIds.value = sortedNotices.value.map((notice) => notice.noticeId)
  } else {
    selectedIds.value = []
  }
}

function openCreate() {
  editorMode.value = 'create'
  editingId.value = null
  form.title = ''
  form.content = ''
  form.pinned = false
  editorOpen.value = true
}

function startEdit(notice) {
  editorMode.value = 'edit'
  editingId.value = notice.noticeId
  form.title = notice.title
  form.content = notice.content
  form.pinned = notice.pinned
  editorOpen.value = true
}

function closeEditor() {
  editorOpen.value = false
}

async function saveNotice() {
  if (!form.title.trim() || !form.content.trim()) {
    alert('제목과 내용을 모두 입력해 주세요.')
    return
  }

  try {
    const payload = { title: form.title, content: form.content, pinned: form.pinned }
    if (editorMode.value === 'create') await adminApi.createNotice(payload)
    else await adminApi.updateNotice(editingId.value, payload)
    closeEditor()
    await fetchNotices()
  } catch (error) {
    alert(error.response?.data?.message || '공지사항 저장 중 문제가 발생했습니다.')
  }
}

async function removeNotice(noticeId) {
  if (!confirm('이 공지사항을 삭제하시겠어요?')) return
  try {
    await adminApi.deleteNotice(noticeId)
    selectedIds.value = selectedIds.value.filter((id) => id !== noticeId)
    await fetchNotices()
  } catch (error) {
    alert(error.response?.data?.message || '공지사항 삭제 중 문제가 발생했습니다.')
  }
}

async function removeSelected() {
  if (!selectedIds.value.length) return
  if (!confirm(`선택한 공지사항 ${selectedIds.value.length}건을 삭제하시겠어요?`)) return

  try {
    await Promise.all(selectedIds.value.map((id) => adminApi.deleteNotice(id)))
    selectedIds.value = []
    await fetchNotices()
  } catch (error) {
    alert(error.response?.data?.message || '공지사항 일괄 삭제 중 문제가 발생했습니다.')
  }
}

onMounted(fetchNotices)
</script>
