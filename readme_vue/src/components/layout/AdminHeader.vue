<template>
  <header class="sticky top-0 z-40 bg-white/90 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur">
    <div class="mx-auto flex w-full max-w-7xl items-center gap-3 px-4 py-4 lg:px-6">
      <router-link to="/admin" class="shrink-0 text-2xl font-bold tracking-tight text-brand-800">
        ReadMe
      </router-link>

      <nav class="ml-auto flex items-center gap-4 text-sm font-medium text-slate-600">
        <button class="transition hover:text-brand-800" @click="handleLogout">로그아웃</button>
        <router-link class="transition hover:text-brand-800" to="/mypage/password">비밀번호 변경</router-link>
        <router-link class="transition hover:text-brand-800" to="/admin/order/approval">주문 승인 요청</router-link>
        <router-link class="transition hover:text-brand-800" to="/admin/product/stock">재고 조회</router-link>
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
