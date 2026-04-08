<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">QnA</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        배송, 결제, 상품 관련 문의를 남기고 답변 상태를 확인할 수 있습니다.
      </p>
    </header>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between gap-4">
        <div>
          <p class="text-sm font-semibold text-brand-700">Support</p>
          <h2 class="mt-1 text-lg font-bold text-slate-900">문의 등록</h2>
        </div>
        <span class="point-chip">{{ authStore.isLoggedIn ? '작성 가능' : '로그인 필요' }}</span>
      </div>

      <form class="grid gap-4 md:grid-cols-2" @submit.prevent="submitQna">
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

        <label class="block space-y-2 md:col-span-2">
          <span class="text-sm font-medium text-slate-700">제목</span>
          <input
            v-model="form.title"
            class="surface-soft w-full px-4 py-3 text-sm outline-none"
            type="text"
            placeholder="문의 제목을 입력하세요"
          >
        </label>

        <label class="block space-y-2 md:col-span-2">
          <span class="text-sm font-medium text-slate-700">내용</span>
          <textarea
            v-model="form.content"
            class="surface-soft min-h-32 w-full px-4 py-3 text-sm outline-none"
            placeholder="문의 내용을 자세히 적어주세요"
          ></textarea>
        </label>

        <p v-if="formMessage" class="md:col-span-2 rounded-2xl px-4 py-3 text-sm" :class="formMessageClass">
          {{ formMessage }}
        </p>

        <div class="md:col-span-2 flex justify-end">
          <button
            type="submit"
            class="rounded-full bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
            :disabled="!authStore.isLoggedIn || submitting"
          >
            {{ submitting ? '등록 중...' : '문의 등록' }}
          </button>
        </div>
      </form>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Question List</p>
          <h2 class="mt-1 text-lg font-bold text-slate-900">공개 문의 목록</h2>
        </div>
        <span class="point-chip">{{ qnas.length }}건</span>
      </div>

      <div v-if="loading" class="py-14 text-center text-sm text-slate-400">QnA 목록을 불러오는 중입니다.</div>

      <div v-else-if="qnas.length === 0" class="surface-soft py-14 text-center text-sm text-slate-400">
        등록된 문의가 없습니다.
      </div>

      <div v-else class="space-y-3">
        <article
          v-for="qna in qnas"
          :key="qna.qnaId"
          class="rounded-[1.5rem] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <div class="flex flex-wrap items-center gap-2">
            <span class="point-chip">{{ qna.category }}</span>
            <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(qna.qnaStatus)">
              {{ statusLabel(qna.qnaStatus) }}
            </span>
          </div>

          <h3 class="mt-4 text-base font-bold text-slate-900">{{ qna.title }}</h3>
          <p class="mt-2 text-sm leading-6 text-slate-600">{{ qna.content }}</p>
          <p class="mt-3 text-xs text-slate-400">
            {{ qna.memberName }} · {{ formatDate(qna.createdAt) }}
          </p>

          <div v-if="qna.answer" class="mt-4 rounded-[1.25rem] bg-brand-50 px-4 py-4">
            <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-700">Answer</p>
            <p class="mt-2 text-sm leading-6 text-slate-700">{{ qna.answer }}</p>
          </div>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { qnaApi } from '@/api/board';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const qnas = ref([]);
const loading = ref(true);
const submitting = ref(false);
const formMessage = ref('');
const formMessageClass = ref('bg-emerald-50 text-emerald-700');

const form = reactive({
  category: '배송',
  title: '',
  content: '',
  isSecret: false,
});

async function fetchQnas() {
  loading.value = true;
  try {
    const { data } = await qnaApi.getList({ page: 0, size: 20, sort: 'createdAt,desc' });
    qnas.value = data.content ?? [];
  } catch (error) {
    console.error('QnA 목록 조회 실패', error);
    qnas.value = [];
  } finally {
    loading.value = false;
  }
}

async function submitQna() {
  if (!authStore.isLoggedIn) {
    formMessage.value = '로그인 후 문의를 등록할 수 있습니다.';
    formMessageClass.value = 'bg-amber-50 text-amber-700';
    return;
  }
  if (!form.title.trim() || !form.content.trim()) {
    formMessage.value = '제목과 내용을 모두 입력해주세요.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
    return;
  }

  submitting.value = true;
  try {
    await qnaApi.create({
      category: form.category,
      title: form.title.trim(),
      content: form.content.trim(),
      isSecret: form.isSecret,
    });
    form.title = '';
    form.content = '';
    form.isSecret = false;
    formMessage.value = '문의가 등록되었습니다.';
    formMessageClass.value = 'bg-emerald-50 text-emerald-700';
    await fetchQnas();
  } catch (error) {
    console.error('QnA 등록 실패', error);
    formMessage.value = error.response?.data?.message || '문의 등록에 실패했습니다.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
  } finally {
    submitting.value = false;
  }
}

function statusLabel(status) {
  return (
    {
      WAITING: '답변 대기',
      PROCESSING: '확인 중',
      COMPLETE: '답변 완료',
    }[status] ?? status
  );
}

function statusClass(status) {
  return (
    {
      WAITING: 'bg-amber-50 text-amber-700',
      PROCESSING: 'bg-sky-50 text-sky-700',
      COMPLETE: 'bg-emerald-50 text-emerald-700',
    }[status] ?? 'bg-slate-100 text-slate-600'
  );
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

onMounted(fetchQnas);
</script>
