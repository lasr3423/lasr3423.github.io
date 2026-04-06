<template>
  <div class="app-wrapper">

    <!-- 헤더 (sticky 고정) -->
    <AppHeader />

    <!-- 중간: 사이드바 + 페이지 콘텐츠 -->
    <div class="content-wrapper">
      <AppSidebar />
      <main class="page-content">
        <!-- 각 뷰 파일의 내용이 여기 들어옴 -->
        <RouterView />
      </main>
    </div>

    <!-- 푸터 -->
    <AppFooter />

  </div>
</template>

<script setup>
import AppHeader  from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppFooter  from '@/components/layout/AppFooter.vue'
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.content-wrapper {
  display: flex;
  gap: 24px;
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 16px;
  flex: 1;
  box-sizing: border-box;
}
.page-content {
  flex: 1;
  min-width: 0; /* flex 자식이 넘치지 않도록 */
}

@media (max-width: 900px) {
  .content-wrapper { flex-direction: column; }
}
</style>