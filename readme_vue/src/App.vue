<template>
  <component :is="currentLayout">
    <router-view />
  </component>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import MainLayout  from '@/layouts/MainLayout.vue';
import AdminLayout from '@/layouts/AdminLayout.vue';
import AuthLayout  from '@/layouts/AuthLayout.vue';

const route = useRoute();

// route.meta.layout 값에 따라 레이아웃 결정
// 'admin' → AdminLayout
// 'auth'  → AuthLayout (헤더/사이드바 없음)
// 기본값  → MainLayout
const currentLayout = computed(() => {
  if (route.meta.layout === 'admin') return AdminLayout;
  if (route.meta.layout === 'auth')  return AuthLayout;
  return MainLayout;
});
</script>

<style>
/* ── 전역 리셋 ──────────────────────────── */
* { box-sizing: border-box; }
body { margin: 0; }

#app {
  width: 100%;
  min-height: 100svh;
  display: flex;
  flex-direction: column;
}

/* ── 공통 레이아웃 구조 ─────────────────── */
.app-root {
  display: flex;
  flex-direction: column;
  min-height: 100svh;
}

/* ── 헤더 ─────────────────────────────── */
.app-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  height: 52px;
  border-bottom: 1px solid #ddd;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-weight: 700;
  font-size: 18px;
  text-decoration: none;
  color: #333;
  white-space: nowrap;
}

.search-input {
  flex: 1;
  max-width: 400px;
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.admin-search {
  width: 180px;
  padding: 5px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 13px;
}

.header-nav {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-nav a {
  font-size: 14px;
  text-decoration: none;
  color: #333;
  padding: 4px 6px;
  border-radius: 4px;
}
.header-nav a:hover { background: #f0f0f0; }

.icon-btn { display: flex; align-items: center; gap: 4px; }

.btn-logout {
  padding: 5px 10px;
  font-size: 13px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.btn-logout:hover { background: #f5f5f5; }

/* ── 바디: 사이드바 + 메인 ─────────────── */
.app-body {
  display: flex;
  flex: 1;
}

/* ── 사이드바 ──────────────────────────── */
.sidebar {
  width: 200px;
  min-width: 200px;
  border-right: 1px solid #ddd;
  background: #fafafa;
  padding: 12px 0;
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 4px;
}

.sidebar-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #444;
  cursor: pointer;
  user-select: none;
}
.sidebar-title:hover { background: #efefef; }

.sidebar-title a {
  text-decoration: none;
  color: #444;
  font-weight: 600;
  font-size: 13px;
  width: 100%;
}

.sidebar ul {
  list-style: none;
  margin: 0;
  padding: 0 0 4px 0;
}

.sidebar ul li a {
  display: block;
  padding: 6px 16px 6px 28px;
  font-size: 13px;
  text-decoration: none;
  color: #555;
}
.sidebar ul li a:hover { background: #e8e8e8; color: #111; }
.sidebar ul li a.router-link-active { color: #5b6ef5; font-weight: 600; }

/* ── 메인 콘텐츠 ────────────────────────── */
.main-content {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
}
</style>
