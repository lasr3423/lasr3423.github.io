<template>
  <header class="app-header">
    <!-- 로고 -->
    <router-link to="/" class="logo">README</router-link>

    <!-- 검색창 -->
    <input class="search-input" type="text" placeholder="도서 검색..." />

    <!-- 헤더 우측 메뉴 -->
    <nav class="header-nav">

      <!-- 비로그인 -->
      <template v-if="!authStore.isLoggedIn">
        <router-link to="/signin">로그인</router-link>
        <router-link to="/signup">회원가입</router-link>
      </template>

      <!-- 일반 유저 로그인 상태 -->
      <template v-else>
        <router-link to="/mypage/edit">회원 정보 수정</router-link>
        <router-link to="/mypage" class="icon-btn" title="마이페이지">👤 마이페이지</router-link>
        <router-link to="/cart" class="icon-btn" title="장바구니">🛒 장바구니</router-link>
        <button class="btn-logout" @click="handleLogout">로그아웃</button>
      </template>

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
