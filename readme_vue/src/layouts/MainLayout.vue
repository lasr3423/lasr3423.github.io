<template>
  <div class="flex min-h-screen flex-col font-sans">

    <!-- 로그인 상태에 따라 헤더 전환 -->
    <GuestHeader v-if="!authStore.isLoggedIn" />
    <UserHeader  v-else />

    <div class="mx-auto flex w-full max-w-[1280px] flex-1 gap-6 px-4 py-6">
      <MainSidebar />
      <main class="min-w-0 flex-1">
        <RouterView />
      </main>
    </div>

    <MainFooter />
  </div>
</template>

<script setup>
import GuestHeader from '@/components/layout/GuestHeader.vue';
import UserHeader  from '@/components/layout/UserHeader.vue';
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MainFooter  from '@/components/layout/MainFooter.vue';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
</script>
