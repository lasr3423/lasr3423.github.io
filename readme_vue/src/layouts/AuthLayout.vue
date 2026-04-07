<template>
  <div class="flex min-h-screen flex-col font-sans">

    <!-- 로그인/회원가입 페이지 → 비회원 헤더 -->
    <GuestHeader />

    <main class="flex flex-1 items-center justify-center px-4 py-12">
      <RouterView />
    </main>

  </div>
</template>

<script setup>
import GuestHeader from '@/components/layout/GuestHeader.vue';
</script>
