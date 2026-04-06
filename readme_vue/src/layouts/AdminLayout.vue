<template>
  <div class="app-root">
    <AdminHeader />
    <div class="app-body">
      <AdminSidebar />
      <main class="main-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import AdminHeader from '@/components/layout/AdminHeader.vue';
import AdminSidebar from '@/components/layout/AdminSidebar.vue';
</script>
