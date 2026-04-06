<template>
  <div class="app-root">
    <MainHeader />
    <div class="app-body">
      <MainSidebar />
      <main class="main-content">
        <slot />
      </main>
    </div>
    <MainFooter />
  </div>
</template>

<script setup>
import MainHeader  from '@/components/layout/MainHeader.vue';
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MainFooter  from '@/components/layout/MainFooter.vue';
</script>
