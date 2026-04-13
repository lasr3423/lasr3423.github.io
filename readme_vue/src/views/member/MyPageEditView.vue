<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Account</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">정보 수정</h1>
      <p class="mt-2 text-sm text-slate-500">이름, 연락처, 주소를 최신 정보로 변경하고 바로 저장할 수 있습니다.</p>
    </section>

    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <form class="max-w-md space-y-5" @submit.prevent="handleSubmit">
        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">이름</span>
          <input v-model="form.name" type="text" required
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            placeholder="이름을 입력하세요" />
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">전화번호</span>
          <input v-model="form.phone" type="tel"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            placeholder="010-0000-0000" />
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">주소</span>
          <input v-model="form.address" type="text"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            placeholder="주소를 입력하세요" />
        </label>

        <p v-if="errorMsg" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ errorMsg }}</p>
        <p v-if="successMsg" class="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ successMsg }}</p>

        <div class="flex gap-3 pt-2">
          <button type="submit" :disabled="loading"
            class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400">
            {{ loading ? '저장 중...' : '저장하기' }}
          </button>
          <router-link to="/mypage"
            class="rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50">
            취소
          </router-link>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { memberApi } from '@/api/member';

const form = ref({ name: '', phone: '', address: '' });
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

onMounted(async () => {
  try {
    const { data } = await memberApi.getMe();
    form.value = { name: data.name || '', phone: data.phone || '', address: data.address || '' };
  } catch (e) {
    errorMsg.value = '정보를 불러오는 데 실패했습니다.';
  }
});

async function handleSubmit() {
  try {
    loading.value = true;
    errorMsg.value = '';
    successMsg.value = '';
    await memberApi.updateMe(form.value);
    successMsg.value = '정보가 성공적으로 수정되었습니다.';
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '수정에 실패했습니다.';
  } finally {
    loading.value = false;
  }
}
</script>
