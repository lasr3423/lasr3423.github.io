<template>
  <section class="space-y-6">
    <header class="surface-panel p-7">
      <p class="text-sm font-semibold text-brand-700">Board</p>
      <h1 class="section-title mt-2">QnA</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        배송, 결제, 상품 관련 문의를 남기고 답변 상태를 확인할 수 있습니다.
      </p>
    </header>

    <section class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <div class="space-y-6">
        <section class="surface-panel p-6">
          <div class="mb-5 flex items-center justify-between gap-4">
            <div>
              <p class="text-sm font-semibold text-brand-700">Support</p>
              <h2 class="mt-1 text-lg font-bold text-slate-900">문의 작성</h2>
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
              <input v-model="form.title" class="surface-soft w-full px-4 py-3 text-sm outline-none" type="text" placeholder="문의 제목을 입력해 주세요">
            </label>

            <label class="block space-y-2 md:col-span-2">
              <span class="text-sm font-medium text-slate-700">내용</span>
              <textarea v-model="form.content" class="surface-soft min-h-32 w-full px-4 py-3 text-sm outline-none" placeholder="문의 내용을 자세히 적어 주세요"></textarea>
            </label>

            <p v-if="formMessage" class="md:col-span-2 rounded-2xl px-4 py-3 text-sm" :class="formMessageClass">
              {{ formMessage }}
            </p>

            <div class="md:col-span-2 flex justify-end gap-3">
              <button
                v-if="editingQnaId"
                type="button"
                class="rounded-full border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                @click="cancelEdit"
              >
                수정 취소
              </button>
              <button
                type="submit"
                class="rounded-full bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                :disabled="!authStore.isLoggedIn || submitting"
              >
                {{ submitting ? (editingQnaId ? '수정 중...' : '등록 중...') : (editingQnaId ? '문의 수정' : '문의 등록') }}
              </button>
            </div>
          </form>
        </section>

        <section class="surface-panel p-6">
          <div class="mb-5 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-brand-700">Question List</p>
              <h2 class="mt-1 text-lg font-bold text-slate-900">문의 목록</h2>
            </div>
            <div class="flex rounded-full bg-slate-100 p-1 text-sm font-semibold">
              <button type="button" class="rounded-full px-4 py-2 transition" :class="activeTab === 'public' ? 'bg-white text-brand-800 shadow-sm' : 'text-slate-500'" @click="activeTab = 'public'">공개 문의</button>
              <button type="button" class="rounded-full px-4 py-2 transition" :class="activeTab === 'my' ? 'bg-white text-brand-800 shadow-sm' : 'text-slate-500'" @click="switchToMyTab" :disabled="!authStore.isLoggedIn">내 문의</button>
            </div>
          </div>

          <div v-if="listLoading" class="py-14 text-center text-sm text-slate-400">QnA 목록을 불러오는 중입니다.</div>

          <div v-else-if="currentQnas.length === 0" class="surface-soft py-14 text-center text-sm text-slate-400">
            {{ activeTab === 'public' ? '등록된 공개 문의가 없습니다.' : '작성한 문의가 없습니다.' }}
          </div>

          <div v-else class="space-y-3">
            <article
              v-for="qna in currentQnas"
              :key="qna.qnaId"
              class="rounded-[1.5rem] border border-slate-200 bg-white p-5 shadow-sm transition hover:border-brand-100"
            >
              <div class="flex flex-wrap items-center justify-between gap-3">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="point-chip">{{ qna.category }}</span>
                  <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(qna.qnaStatus)">
                    {{ statusLabel(qna.qnaStatus) }}
                  </span>
                  <span v-if="qna.secret" class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">비밀글</span>
                </div>
                <button type="button" class="text-sm font-semibold text-brand-700 transition hover:text-brand-800" @click="openQnaDetail(qna)">
                  상세 보기
                </button>
              </div>

              <h3 class="mt-4 text-base font-bold text-slate-900">{{ qna.title }}</h3>
              <p class="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">{{ qna.content }}</p>
              <p class="mt-3 text-xs text-slate-400">{{ qna.memberName }} · {{ formatDate(qna.createdAt) }}</p>

              <div v-if="qna.answer" class="mt-4 rounded-[1.25rem] bg-brand-50 px-4 py-4">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-700">Answer</p>
                <p class="mt-2 text-sm leading-6 text-slate-700">{{ qna.answer }}</p>
              </div>
            </article>
          </div>
        </section>
      </div>

      <aside class="surface-panel p-6">
        <template v-if="detailLoading">
          <div class="flex min-h-[420px] items-center justify-center text-sm text-slate-400">문의 상세를 불러오는 중입니다.</div>
        </template>

        <template v-else-if="selectedQna">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="flex flex-wrap items-center gap-2">
                <span class="point-chip">{{ selectedQna.category }}</span>
                <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(selectedQna.qnaStatus)">
                  {{ statusLabel(selectedQna.qnaStatus) }}
                </span>
              </div>
              <h2 class="mt-3 text-2xl font-bold tracking-tight text-slate-900">{{ selectedQna.title }}</h2>
            </div>
            <span class="text-sm text-slate-400">{{ formatDate(selectedQna.createdAt) }}</span>
          </div>

          <dl class="mt-5 grid gap-3 sm:grid-cols-3">
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">작성자</dt>
              <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedQna.memberName }}</dd>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">상태</dt>
              <dd class="mt-2 text-sm font-semibold text-slate-900">{{ statusLabel(selectedQna.qnaStatus) }}</dd>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-slate-500">조회수</dt>
              <dd class="mt-2 text-sm font-semibold text-slate-900">{{ selectedQna.viewCount }}</dd>
            </div>
          </dl>

          <div class="mt-6 rounded-[1.5rem] bg-slate-50 px-5 py-6 text-sm leading-7 text-slate-700">
            {{ selectedQna.content }}
          </div>

          <div v-if="selectedAnswer" class="mt-6 rounded-[1.5rem] border border-brand-100 bg-brand-50/70 px-5 py-6">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Answer</p>
            <p class="mt-3 text-sm leading-7 text-slate-700">{{ selectedAnswer.content }}</p>
            <p class="mt-3 text-xs text-slate-500">답변일 {{ formatDate(selectedAnswer.createdAt) }}</p>
          </div>

          <div v-if="canManageSelectedQna" class="mt-6 flex gap-3">
            <button type="button" class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50" @click="startEditSelectedQna">
              문의 수정
            </button>
            <button type="button" class="rounded-2xl bg-rose-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-rose-600" @click="deleteSelectedQna">
              문의 삭제
            </button>
          </div>
        </template>

        <div v-else class="flex min-h-[420px] items-center justify-center rounded-[1.5rem] bg-slate-50 px-6 text-center text-sm text-slate-500">
          목록에서 문의를 선택하면 상세 내용과 답변이 표시됩니다.
        </div>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { qnaApi } from '@/api/board';
import { memberApi } from '@/api/member';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const activeTab = ref('public');
const publicQnas = ref([]);
const myQnas = ref([]);
const selectedQna = ref(null);
const currentMemberId = ref(null);
const listLoading = ref(true);
const detailLoading = ref(false);
const submitting = ref(false);
const editingQnaId = ref(null);
const formMessage = ref('');
const formMessageClass = ref('bg-emerald-50 text-emerald-700');

const form = reactive({
  category: '배송',
  title: '',
  content: '',
  isSecret: false,
});

const currentQnas = computed(() => (activeTab.value === 'public' ? publicQnas.value : myQnas.value));
const selectedAnswer = computed(() => selectedQna.value?.children?.find((child) => child.depth > selectedQna.value.depth) || null);
const canManageSelectedQna = computed(() => {
  if (!selectedQna.value || !currentMemberId.value) return false;
  return selectedQna.value.memberId === currentMemberId.value && !selectedAnswer.value;
});

async function initialize() {
  if (authStore.isLoggedIn) {
    try {
      const { data } = await memberApi.getMe();
      currentMemberId.value = data.id;
    } catch (error) {
      console.error('회원 정보 조회 실패', error);
    }
  }
  await Promise.all([fetchPublicQnas(), fetchMyQnas()]);
}

async function fetchPublicQnas() {
  listLoading.value = true;
  try {
    const { data } = await qnaApi.getList({ page: 0, size: 20, sort: 'createdAt,desc' });
    publicQnas.value = data.content ?? [];
    if (!selectedQna.value && publicQnas.value[0]) {
      await openQnaDetail(publicQnas.value[0]);
    }
  } catch (error) {
    console.error('공개 QnA 목록 조회 실패', error);
    publicQnas.value = [];
  } finally {
    listLoading.value = false;
  }
}

async function fetchMyQnas() {
  if (!authStore.isLoggedIn) {
    myQnas.value = [];
    return;
  }

  try {
    const { data } = await qnaApi.getMyList({ page: 0, size: 20, sort: 'createdAt,desc' });
    myQnas.value = data.content ?? [];
  } catch (error) {
    console.error('내 문의 목록 조회 실패', error);
    myQnas.value = [];
  }
}

async function openQnaDetail(qna) {
  detailLoading.value = true;
  try {
    const fetcher = activeTab.value === 'my' || qna.secret ? qnaApi.getMyDetail : qnaApi.getDetail;
    const { data } = await fetcher(qna.qnaId);
    selectedQna.value = data;
  } catch (error) {
    console.error('QnA 상세 조회 실패', error);
    formMessage.value = error.response?.data?.message || '문의 상세를 불러오지 못했습니다.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
  } finally {
    detailLoading.value = false;
  }
}

async function submitQna() {
  if (!authStore.isLoggedIn) {
    formMessage.value = '로그인 후 문의를 등록할 수 있습니다.';
    formMessageClass.value = 'bg-amber-50 text-amber-700';
    return;
  }
  if (!form.title.trim() || !form.content.trim()) {
    formMessage.value = '제목과 내용을 모두 입력해 주세요.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
    return;
  }

  submitting.value = true;
  try {
    if (editingQnaId.value) {
      await qnaApi.updateMy(editingQnaId.value, {
        title: form.title.trim(),
        content: form.content.trim(),
      });
      formMessage.value = '문의가 수정되었습니다.';
    } else {
      await qnaApi.create({
        category: form.category,
        title: form.title.trim(),
        content: form.content.trim(),
        isSecret: form.isSecret,
      });
      formMessage.value = '문의가 등록되었습니다.';
    }
    formMessageClass.value = 'bg-emerald-50 text-emerald-700';
    cancelEdit();
    await Promise.all([fetchPublicQnas(), fetchMyQnas()]);
    if (myQnas.value[0]) {
      activeTab.value = 'my';
      await openQnaDetail(myQnas.value[0]);
    }
  } catch (error) {
    console.error('QnA 저장 실패', error);
    formMessage.value = error.response?.data?.message || '문의 저장에 실패했습니다.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
  } finally {
    submitting.value = false;
  }
}

function startEditSelectedQna() {
  if (!selectedQna.value) return;
  editingQnaId.value = selectedQna.value.qnaId;
  form.category = selectedQna.value.category || '배송';
  form.title = selectedQna.value.title;
  form.content = selectedQna.value.content;
  form.isSecret = !!selectedQna.value.secret;
  formMessage.value = '문의 수정 모드입니다. 내용을 바꾼 뒤 저장해 주세요.';
  formMessageClass.value = 'bg-sky-50 text-sky-700';
}

function cancelEdit() {
  editingQnaId.value = null;
  form.category = '배송';
  form.title = '';
  form.content = '';
  form.isSecret = false;
}

async function deleteSelectedQna() {
  if (!selectedQna.value || !confirm('선택한 문의를 삭제하시겠습니까?')) return;
  try {
    await qnaApi.removeMy(selectedQna.value.qnaId);
    formMessage.value = '문의가 삭제되었습니다.';
    formMessageClass.value = 'bg-emerald-50 text-emerald-700';
    selectedQna.value = null;
    cancelEdit();
    await Promise.all([fetchPublicQnas(), fetchMyQnas()]);
  } catch (error) {
    formMessage.value = error.response?.data?.message || '문의 삭제에 실패했습니다.';
    formMessageClass.value = 'bg-rose-50 text-rose-700';
  }
}

async function switchToMyTab() {
  if (!authStore.isLoggedIn) return;
  activeTab.value = 'my';
  if (!myQnas.value.length) {
    await fetchMyQnas();
  }
  if (myQnas.value[0]) {
    await openQnaDetail(myQnas.value[0]);
  } else {
    selectedQna.value = null;
  }
}

function statusLabel(status) {
  return ({ WAITING: '답변 대기', PROCESSING: '확인 중', COMPLETE: '답변 완료' }[status] ?? status);
}

function statusClass(status) {
  return ({
    WAITING: 'bg-amber-50 text-amber-700',
    PROCESSING: 'bg-sky-50 text-sky-700',
    COMPLETE: 'bg-emerald-50 text-emerald-700',
  }[status] ?? 'bg-slate-100 text-slate-600');
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

onMounted(initialize);
</script>
