<template>
  <div class="flex min-h-screen flex-col font-sans">
    <GuestHeader v-if="!authStore.isLoggedIn" />
    <UserHeader v-else />

    <div class="app-main-grid">
      <MainSidebar />
      <main class="content-stack">
        <RouterView />
      </main>
    </div>

    <MainFooter />
  </div>
</template>

<script setup>
import GuestHeader from '@/components/layout/GuestHeader.vue';
import UserHeader from '@/components/layout/UserHeader.vue';
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MainFooter from '@/components/layout/MainFooter.vue';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
</script>
