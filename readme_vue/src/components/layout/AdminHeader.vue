<template>
  <header class="sticky top-0 z-40 border-b border-slate-200/80 bg-slate-950 text-slate-100 shadow-lg">
    <div class="mx-auto flex w-full max-w-7xl items-center gap-3 px-4 py-3 lg:px-6">
      <router-link to="/admin" class="shrink-0 text-lg font-bold tracking-tight text-white">
        README 관리자
      </router-link>

      <input
        class="hidden w-full max-w-xs rounded-2xl border border-slate-700 bg-slate-900 px-4 py-2 text-sm text-slate-100 outline-none placeholder:text-slate-500 md:block"
        type="text"
        placeholder="회원/주문 검색..."
      >

      <nav class="ml-auto flex items-center gap-2 text-sm">
        <router-link class="rounded-full border border-slate-700 px-4 py-2 text-slate-200 transition hover:border-brand-300 hover:bg-slate-900" to="/admin/order/approval">
          주문 승인
        </router-link>
        <router-link class="rounded-full border border-slate-700 px-4 py-2 text-slate-200 transition hover:border-brand-300 hover:bg-slate-900" to="/admin/product/stock">
          재고 관리
        </router-link>
        <button
          class="rounded-full bg-accent-500 px-4 py-2 font-semibold text-white transition hover:bg-accent-600"
          @click="handleLogout"
        >
          로그아웃
        </button>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const router = useRouter();

async function handleLogout() {
  await authStore.signout();
  alert('로그아웃 되었습니다.');
  router.push('/');
}
</script>
