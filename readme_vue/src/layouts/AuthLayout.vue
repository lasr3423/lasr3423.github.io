<template>
  <div class="flex min-h-screen flex-col">
    <MainHeader />
    <main class="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-10 lg:px-6">
      <slot />
    </main>
  </div>
</template>

<script setup>
import MainHeader from '@/components/layout/MainHeader.vue';
</script>
