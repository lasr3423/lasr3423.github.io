<template>
  <header class="app-header">
    <!-- 로고 -->
    <router-link to="/admin" class="logo">README 관리자</router-link>

    <!-- 관리자 회원 검색 -->
    <input class="admin-search" type="text" placeholder="회원 검색..." />

    <!-- 헤더 우측 메뉴 -->
    <nav class="header-nav">
      <router-link to="/admin/order/approval" class="icon-btn" title="주문 승인 요청">📋 주문 승인</router-link>
      <router-link to="/admin/product/stock" class="icon-btn" title="재고 목록">📦 재고 목록</router-link>
      <button class="btn-logout" @click="handleLogout">로그아웃</button>
    </nav>
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
