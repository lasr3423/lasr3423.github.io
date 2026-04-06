<template>
  <div>
    <h2>README 쇼핑몰</h2>

    <!-- 비로그인 -->
    <template v-if="!authStore.isLoggedIn">
      <p>로그인하거나 회원가입 후 이용해주세요.</p>
      <router-link to="/signin">로그인</router-link>
      &nbsp;
      <router-link to="/signup">회원가입</router-link>
    </template>

    <!-- 일반 유저 -->
    <template v-else-if="!authStore.isAdmin">
      <p>환영합니다! 왼쪽 메뉴에서 원하는 기능을 선택해주세요.</p>
      <p style="font-size:13px; color:#888;">현재 역할: {{ authStore.userRole }}</p>
    </template>

    <!-- 관리자 -->
    <template v-else>
      <p>관리자 페이지입니다. 왼쪽 메뉴에서 관리 기능을 선택해주세요.</p>
      <p style="font-size:13px; color:#888;">현재 역할: {{ authStore.userRole }}</p>
      <router-link to="/admin">대시보드로 이동</router-link>
    </template>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/auth';
const authStore = useAuthStore();
</script>
