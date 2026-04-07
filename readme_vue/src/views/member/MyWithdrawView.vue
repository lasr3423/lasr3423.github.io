<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-rose-600">Danger Zone</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">회원 탈퇴</h1>
    </section>

    <section class="rounded-[2rem] border border-rose-200 bg-white p-8 shadow-sm">
      <div class="max-w-md space-y-6">
        <div class="rounded-2xl border border-rose-100 bg-rose-50 p-5 text-sm leading-6 text-rose-700">
          <p class="font-semibold">탈퇴 전 꼭 확인하세요</p>
          <ul class="mt-2 list-disc space-y-1 pl-4">
            <li>탈퇴 시 모든 주문 내역과 계정 정보가 삭제됩니다.</li>
            <li>삭제된 계정은 복구할 수 없습니다.</li>
            <li>동일한 이메일로 재가입이 가능합니다.</li>
          </ul>
        </div>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">확인을 위해 아래에 <strong>"탈퇴합니다"</strong>를 입력하세요</span>
          <input v-model="confirmText" type="text"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-400 focus:bg-white focus:ring-4 focus:ring-rose-100"
            placeholder="탈퇴합니다" />
        </label>

        <p v-if="errorMsg" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ errorMsg }}</p>

        <div class="flex gap-3">
          <button
            :disabled="confirmText !== '탈퇴합니다' || loading"
            class="rounded-2xl bg-rose-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:bg-slate-300"
            @click="handleWithdraw"
          >
            {{ loading ? '처리 중...' : '탈퇴하기' }}
          </button>
          <router-link to="/mypage"
            class="rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50">
            취소
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { memberApi } from '@/api/member';

const router = useRouter();
const authStore = useAuthStore();
const confirmText = ref('');
const loading = ref(false);
const errorMsg = ref('');

async function handleWithdraw() {
  try {
    loading.value = true;
    errorMsg.value = '';
    await memberApi.withdraw();
    authStore.clearTokens();
    router.push('/');
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '탈퇴 처리 중 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
}
</script>
