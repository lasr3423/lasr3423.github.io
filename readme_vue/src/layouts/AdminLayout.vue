<template>
  <div class="flex min-h-screen flex-col bg-slate-100/80">
    <AdminHeader />
    <div class="mx-auto flex w-full max-w-7xl flex-1 gap-6 px-4 py-6 lg:px-6">
      <AdminSidebar />
      <main class="min-w-0 flex-1">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import AdminHeader from '@/components/layout/AdminHeader.vue';
import AdminSidebar from '@/components/layout/AdminSidebar.vue';
</script>
