<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">공지사항</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        서비스 운영 공지와 이벤트 소식을 확인하실 수 있습니다.
      </p>
    </header>

    <section class="surface-panel overflow-hidden">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
        <div class="flex items-center gap-2">
          <span class="point-chip">공지</span>
          <span class="text-sm text-slate-500">전체 {{ totalElements.toLocaleString() }}건</span>
        </div>
      </div>

      <div v-if="loading" class="px-6 py-16 text-center text-sm text-slate-400">공지사항을 불러오는 중입니다.</div>

      <div v-else-if="notices.length === 0" class="px-6 py-16 text-center text-sm text-slate-400">
        등록된 공지사항이 없습니다.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[920px] table-fixed text-sm">
          <colgroup>
            <col class="w-16">
            <col class="w-28">
            <col>
            <col class="w-28">
            <col class="w-28">
            <col class="w-24">
          </colgroup>
          <thead class="border-b border-slate-200 bg-slate-50">
            <tr class="text-slate-500">
              <th class="px-4 py-3 text-center font-semibold">No</th>
              <th class="px-4 py-3 text-center font-semibold">구분</th>
              <th class="px-4 py-3 text-left font-semibold">제목</th>
              <th class="px-4 py-3 text-center font-semibold">작성자</th>
              <th class="px-4 py-3 text-center font-semibold">작성일</th>
              <th class="px-4 py-3 text-center font-semibold">조회</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="(notice, index) in notices"
              :key="notice.noticeId"
              class="transition hover:bg-slate-50"
            >
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ totalElements - (page * pageSize) - index }}</td>
              <td class="px-4 py-4 text-center">
                <span class="inline-flex max-w-full truncate rounded-full px-2.5 py-1 text-xs font-semibold" :class="notice.pinned ? 'bg-accent-50 text-accent-600' : 'bg-slate-100 text-slate-600'">
                  {{ notice.pinned ? '중요' : '일반' }}
                </span>
              </td>
              <td class="px-4 py-4">
                <button
                  type="button"
                  class="flex w-full min-w-0 items-center gap-2 text-left"
                  @click="selectNotice(notice.noticeId)"
                >
                  <span v-if="notice.pinned" class="shrink-0 rounded-full bg-brand-50 px-2 py-0.5 text-[11px] font-semibold text-brand-700">고정</span>
                  <span class="card-title-1 block min-w-0 font-medium text-slate-900 hover:text-brand-800">{{ notice.title }}</span>
                </button>
              </td>
              <td class="px-4 py-4 text-center text-slate-500">{{ notice.authorName || '관리자' }}</td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ formatDate(notice.createdAt) }}</td>
              <td class="numeric-stable px-4 py-4 text-center text-slate-500">{{ notice.viewCount }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 border-t border-slate-100 px-6 py-4">
        <button type="button" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" :disabled="page === 0" @click="changePage(page - 1)">이전</button>
        <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
        <button type="button" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" :disabled="page >= totalPages - 1" @click="changePage(page + 1)">다음</button>
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
            <p class="point-chip">공지사항</p>
            <h2 class="mt-3 text-xl font-bold text-slate-900">{{ selectedNotice?.title || '공지 상세' }}</h2>
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
          <div v-if="detailLoading" class="py-16 text-center text-sm text-slate-400">
            공지 상세를 불러오는 중입니다.
          </div>

          <template v-else-if="selectedNotice">
            <div class="flex flex-wrap items-center gap-2">
              <span v-if="selectedNotice.pinned" class="point-chip">중요 공지</span>
              <span class="ml-auto text-sm text-slate-400">{{ formatDate(selectedNotice.createdAt) }}</span>
            </div>

            <dl class="mt-5 grid gap-3 sm:grid-cols-3">
              <div class="rounded-2xl bg-slate-50 p-4">
                <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</dt>
                <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedNotice.authorName || '관리자' }}</dd>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">등록일</dt>
                <dd class="mt-2 text-sm font-semibold text-slate-900">{{ formatDate(selectedNotice.createdAt) }}</dd>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">조회수</dt>
                <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedNotice.viewCount }}</dd>
              </div>
            </dl>

            <div class="mt-6 rounded-[1.5rem] bg-slate-50 px-5 py-6 text-sm leading-7 text-slate-700 whitespace-pre-line">
              {{ selectedNotice.content }}
            </div>
          </template>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { noticeApi } from '@/api/board'

const notices = ref([])
const selectedNotice = ref(null)
const loading = ref(true)
const detailLoading = ref(false)
const modalOpen = ref(false)
const page = ref(0)
const pageSize = 12
const totalPages = ref(1)
const totalElements = ref(0)

async function fetchNotices() {
  loading.value = true
  try {
    const { data } = await noticeApi.getList({ page: page.value, size: pageSize, sort: 'createdAt,desc' })
    notices.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || notices.value.length
  } catch (error) {
    console.error('공지사항 조회 실패', error)
    notices.value = []
  } finally {
    loading.value = false
  }
}

async function selectNotice(noticeId) {
  modalOpen.value = true
  detailLoading.value = true
  try {
    const { data } = await noticeApi.getDetail(noticeId)
    selectedNotice.value = data
  } catch (error) {
    console.error('공지사항 상세 조회 실패', error)
    selectedNotice.value = null
  } finally {
    detailLoading.value = false
  }
}

function closeModal() {
  modalOpen.value = false
  selectedNotice.value = null
}

async function changePage(nextPage) {
  page.value = nextPage
  await fetchNotices()
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

onMounted(fetchNotices)
</script>
