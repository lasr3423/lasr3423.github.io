<template>
  <div class="admin-root">
    <AdminHeader />
    <div class="admin-body">
      <AdminSidebar />
      <main class="admin-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import AdminHeader from '@/components/layout/AdminHeader.vue';
import AdminSidebar from '@/components/layout/AdminSidebar.vue';
</script>

<style scoped>
.admin-root {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: var(--font-sans);
  background: var(--color-white2);
}

.admin-body {
  display: flex;
  flex: 1;
}

.admin-content {
  flex: 1;
  min-width: 0;
  padding: var(--space-3);
}
</style>