<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Account</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">비밀번호 변경</h1>
    </section>

    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <form class="max-w-md space-y-5" @submit.prevent="handleSubmit">
        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">현재 비밀번호</span>
          <input v-model="form.currentPassword" type="password" required
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            placeholder="현재 비밀번호" />
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">새 비밀번호</span>
          <input v-model="form.newPassword" type="password" required
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            placeholder="새 비밀번호 (8자 이상)" />
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">새 비밀번호 확인</span>
          <input v-model="form.confirmPassword" type="password" required
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            placeholder="새 비밀번호 재입력" />
        </label>

        <p v-if="errorMsg"   class="rounded-2xl border border-rose-200  bg-rose-50  px-4 py-3 text-sm text-rose-700">{{ errorMsg }}</p>
        <p v-if="successMsg" class="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ successMsg }}</p>

        <div class="flex gap-3 pt-2">
          <button type="submit" :disabled="loading"
            class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400">
            {{ loading ? '변경 중...' : '비밀번호 변경' }}
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
import { ref } from 'vue';
import { memberApi } from '@/api/member';

const form = ref({ currentPassword: '', newPassword: '', confirmPassword: '' });
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

async function handleSubmit() {
  if (form.value.newPassword !== form.value.confirmPassword) {
    errorMsg.value = '새 비밀번호가 일치하지 않습니다.';
    return;
  }
  try {
    loading.value = true;
    errorMsg.value = '';
    successMsg.value = '';
    await memberApi.changePassword(form.value);
    successMsg.value = '비밀번호가 변경되었습니다.';
    form.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '비밀번호 변경에 실패했습니다.';
  } finally {
    loading.value = false;
  }
}
</script>
