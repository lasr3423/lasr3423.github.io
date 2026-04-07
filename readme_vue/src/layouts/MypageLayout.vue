<template>
  <div class="mypage-root">
    <MyHeader />
    <div class="mypage-body">
      <MySidebar />
      <main class="mypage-content">
        <RouterView />
      </main>
    </div>
    <MyFooter />
  </div>
</template>

<script setup>
import MyHeader  from '@/components/layout/MyHeader.vue';
import MySidebar from '@/components/layout/MySidebar.vue';
import MyFooter  from '@/components/layout/MyFooter.vue';
</script>

<style scoped>
.mypage-root {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: var(--font-sans);
}

.mypage-body {
  display: flex;
  gap: var(--space-3);
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: var(--space-3) var(--space-2);
  box-sizing: border-box;
}

.mypage-content {
  flex: 1;
  min-width: 0;
}
</style>