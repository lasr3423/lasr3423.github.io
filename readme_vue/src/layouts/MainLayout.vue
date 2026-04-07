<template>
  <div class="flex min-h-screen flex-col">
    <MainHeader />
    <div class="mx-auto flex w-full max-w-7xl flex-1 gap-6 px-4 py-6 lg:px-6">
      <MainSidebar />
      <main class="min-w-0 flex-1">
        <slot />
      </main>
    </div>
    <MainFooter />
  </div>
</template>

<script setup>
import MainHeader from '@/components/layout/MainHeader.vue';
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MainFooter from '@/components/layout/MainFooter.vue';
</script>
