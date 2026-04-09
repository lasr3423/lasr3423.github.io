<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">공지사항</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        서비스 운영 공지와 이벤트 소식을 한 곳에서 확인할 수 있습니다.
      </p>
    </header>

    <section class="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
      <div class="surface-panel p-6">
        <div class="mb-5 flex items-center justify-between">
          <div>
            <p class="text-sm font-semibold text-brand-700">Notice List</p>
            <h2 class="mt-1 text-lg font-bold text-slate-900">전체 공지</h2>
          </div>
          <span class="point-chip">{{ totalElements.toLocaleString() }}건</span>
        </div>

        <div v-if="loading" class="py-14 text-center text-sm text-slate-400">공지사항을 불러오는 중입니다.</div>

        <div v-else-if="notices.length === 0" class="surface-soft py-14 text-center text-sm text-slate-400">
          등록된 공지사항이 없습니다.
        </div>

        <div v-else class="space-y-3">
          <button
            v-for="notice in notices"
            :key="notice.noticeId"
            class="w-full rounded-[1.5rem] border px-5 py-4 text-left transition"
            :class="selectedNotice?.noticeId === notice.noticeId
              ? 'border-brand-200 bg-brand-50/60 shadow-sm'
              : 'border-slate-200 bg-white hover:border-brand-100 hover:bg-slate-50'"
            @click="selectNotice(notice.noticeId)"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <span v-if="notice.pinned" class="point-chip">고정</span>
                  <p class="truncate text-base font-semibold text-slate-900">{{ notice.title }}</p>
                </div>
                <p class="mt-2 text-sm text-slate-500">
                  {{ formatDate(notice.createdAt) }} · {{ notice.authorName || '관리자' }}
                </p>
              </div>
              <span class="text-xs font-medium text-slate-400">조회 {{ notice.viewCount }}</span>
            </div>
          </button>
        </div>

        <div v-if="totalPages > 1" class="mt-5 flex items-center justify-center gap-2">
          <button type="button" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" :disabled="page === 0" @click="changePage(page - 1)">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button type="button" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" :disabled="page >= totalPages - 1" @click="changePage(page + 1)">다음</button>
        </div>
      </div>

      <section class="surface-panel p-6">
        <template v-if="detailLoading">
          <div class="flex min-h-[360px] items-center justify-center text-sm text-slate-400">
            공지 상세를 불러오는 중입니다.
          </div>
        </template>

        <template v-else-if="selectedNotice">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="flex flex-wrap items-center gap-2">
                <span v-if="selectedNotice.pinned" class="point-chip">중요 공지</span>
                <p class="text-sm font-semibold text-brand-700">Notice Detail</p>
              </div>
              <h2 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">{{ selectedNotice.title }}</h2>
            </div>
            <span class="text-sm text-slate-400">{{ formatDate(selectedNotice.createdAt) }}</span>
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

          <div class="mt-6 rounded-[1.5rem] bg-slate-50 px-5 py-6 text-sm leading-7 text-slate-700">
            {{ selectedNotice.content }}
          </div>
        </template>

        <div v-else class="flex min-h-[360px] items-center justify-center rounded-[1.5rem] bg-slate-50 px-6 text-center text-sm text-slate-500">
          왼쪽 목록에서 공지사항을 선택하면 상세 내용이 표시됩니다.
        </div>
      </section>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { noticeApi } from '@/api/board';

const notices = ref([]);
const selectedNotice = ref(null);
const loading = ref(true);
const detailLoading = ref(false);
const page = ref(0);
const totalPages = ref(1);
const totalElements = ref(0);

async function fetchNotices() {
  loading.value = true;
  try {
    const { data } = await noticeApi.getList({ page: page.value, size: 12, sort: 'createdAt,desc' });
    notices.value = data.content ?? [];
    totalPages.value = data.totalPages || 1;
    totalElements.value = data.totalElements || notices.value.length;

    if (!selectedNotice.value && notices.value[0]) {
      await selectNotice(notices.value[0].noticeId);
    }
  } catch (error) {
    console.error('공지사항 조회 실패', error);
    notices.value = [];
  } finally {
    loading.value = false;
  }
}

async function selectNotice(noticeId) {
  detailLoading.value = true;
  try {
    const { data } = await noticeApi.getDetail(noticeId);
    selectedNotice.value = data;
  } catch (error) {
    console.error('공지사항 상세 조회 실패', error);
  } finally {
    detailLoading.value = false;
  }
}

async function changePage(nextPage) {
  page.value = nextPage;
  selectedNotice.value = null;
  await fetchNotices();
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

onMounted(fetchNotices);
</script>
