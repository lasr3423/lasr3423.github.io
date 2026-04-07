<template>
  <div class="flex min-h-screen flex-col bg-slate-100 font-sans">

    <!-- 관리자 전용 헤더 -->
    <AdminHeader />

    <div class="flex flex-1">
      <AdminSidebar />
      <main class="min-w-0 flex-1 p-6">
        <RouterView />
      </main>
    </div>

    <!-- 관리자 페이지는 푸터 없음 -->
  </div>
</template>

<script setup>
import AdminHeader  from '@/components/layout/AdminHeader.vue';
import AdminSidebar from '@/components/layout/AdminSidebar.vue';
</script>
