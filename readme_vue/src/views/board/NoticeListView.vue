<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">공지사항</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        서비스 운영 안내와 이벤트 공지를 한 곳에서 확인할 수 있습니다.
      </p>
    </header>

    <section class="surface-panel p-6">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Pinned</p>
          <h2 class="mt-1 text-lg font-bold text-slate-900">중요 공지</h2>
        </div>
        <span class="point-chip">{{ notices.length }}건</span>
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
            <div>
              <div class="flex items-center gap-2">
                <span v-if="notice.pinned" class="point-chip">고정</span>
                <p class="text-base font-semibold text-slate-900">{{ notice.title }}</p>
              </div>
              <p class="mt-2 text-sm text-slate-500">
                {{ formatDate(notice.createdAt) }} · {{ notice.authorName || '관리자' }}
              </p>
            </div>
            <span class="text-xs font-medium text-slate-400">조회 {{ notice.viewCount }}</span>
          </div>
        </button>
      </div>
    </section>

    <section v-if="selectedNotice" class="surface-panel p-6">
      <div class="flex items-center justify-between gap-4">
        <div>
          <p class="text-sm font-semibold text-brand-700">Detail</p>
          <h2 class="mt-1 text-xl font-bold text-slate-900">{{ selectedNotice.title }}</h2>
        </div>
        <span class="text-sm text-slate-400">{{ formatDate(selectedNotice.createdAt) }}</span>
      </div>

      <div class="mt-6 rounded-[1.5rem] bg-slate-50 px-5 py-6 text-sm leading-7 text-slate-700">
        {{ selectedNotice.content }}
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { noticeApi } from '@/api/board';

const notices = ref([]);
const selectedNotice = ref(null);
const loading = ref(true);

async function fetchNotices() {
  loading.value = true;
  try {
    const { data } = await noticeApi.getList({ page: 0, size: 20, sort: 'createdAt,desc' });
    notices.value = data.content ?? [];

    const firstPinned = notices.value.find((notice) => notice.pinned);
    if (firstPinned) {
      await selectNotice(firstPinned.noticeId);
    } else if (notices.value[0]) {
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
  try {
    const { data } = await noticeApi.getDetail(noticeId);
    selectedNotice.value = data;
  } catch (error) {
    console.error('공지사항 상세 조회 실패', error);
  }
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
