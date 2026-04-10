<template>
  <div class="flex min-h-screen flex-col font-sans">
    <UserHeader />

    <div class="app-main-grid">
      <MySidebar />
      <main class="content-stack">
        <RouterView />
      </main>
    </div>

    <MainFooter />
  </div>
</template>

<script setup>
import UserHeader from '@/components/layout/UserHeader.vue';
import MySidebar from '@/components/layout/MySidebar.vue';
import MainFooter from '@/components/layout/MainFooter.vue';
</script>
