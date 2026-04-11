<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <div v-if="detailLoading" class="py-20 text-center text-sm text-slate-400">공지 상세를 불러오는 중입니다.</div>

      <template v-else-if="notice">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div class="flex flex-wrap items-center gap-2">
              <span v-if="notice.pinned" class="point-chip">고정 공지</span>
              <p class="text-sm font-semibold text-brand-700">Notice Detail</p>
            </div>
            <h1 class="section-title mt-3">{{ notice.title }}</h1>
          </div>
          <button
            type="button"
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="router.push('/notice')"
          >
            목록으로
          </button>
        </div>

        <dl class="mt-6 grid gap-3 sm:grid-cols-3">
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">{{ notice.authorName || '관리자' }}</dd>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성일</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">{{ formatDate(notice.createdAt) }}</dd>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">조회수</dt>
            <dd class="mt-2 text-sm font-semibold text-slate-900">{{ notice.viewCount }}</dd>
          </div>
        </dl>

        <div class="mt-6 rounded-[1.75rem] bg-slate-50 px-6 py-7 text-sm leading-7 text-slate-700">
          {{ notice.content }}
        </div>
      </template>

      <div v-else class="py-20 text-center text-sm text-slate-400">공지사항을 찾을 수 없습니다.</div>
    </header>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { noticeApi } from '@/api/board'

const route = useRoute()
const router = useRouter()
const notice = ref(null)
const detailLoading = ref(true)

async function fetchNotice() {
  detailLoading.value = true
  try {
    const { data } = await noticeApi.getDetail(route.params.noticeId)
    notice.value = data
  } catch (error) {
    console.error('공지사항 상세 조회 실패', error)
    notice.value = null
  } finally {
    detailLoading.value = false
  }
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

onMounted(fetchNotice)
</script>
