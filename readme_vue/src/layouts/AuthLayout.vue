<template>
  <div class="auth-root">
    <AppHeader />
    <main class="auth-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue';
</script>

<style scoped>
.auth-root {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: var(--font-sans);
}

.auth-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5) var(--space-2);
}
</style>
