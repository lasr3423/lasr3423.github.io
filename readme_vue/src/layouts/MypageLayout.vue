<template>
  <div class="flex min-h-screen flex-col font-sans">

    <!-- 마이페이지는 항상 로그인 상태 → UserHeader 고정 -->
    <UserHeader />

    <div class="mx-auto flex w-full max-w-[1280px] flex-1 gap-6 px-4 py-6">
      <MySidebar />
      <main class="min-w-0 flex-1">
        <RouterView />
      </main>
    </div>

    <MainFooter />
  </div>
</template>

<script setup>
import UserHeader  from '@/components/layout/UserHeader.vue';
import MySidebar   from '@/components/layout/MySidebar.vue';
import MainFooter  from '@/components/layout/MainFooter.vue';
</script>
