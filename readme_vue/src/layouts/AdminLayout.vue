<template>
  <div class="flex min-h-screen flex-col bg-slate-100 font-sans">
    <AdminHeader />

    <div class="app-shell flex flex-1 items-start gap-6 py-6 lg:gap-8 lg:py-8">
      <AdminSidebar />
      <main class="content-stack rounded-[2rem] bg-white/60 p-0">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import AdminHeader from '@/components/layout/AdminHeader.vue';
import AdminSidebar from '@/components/layout/AdminSidebar.vue';
</script>
