<template>
  <div class="app-root">

    <!-- ===== 헤더 ===== -->
    <header class="app-header">
      <!-- 로고 -->
      <router-link to="/" class="logo">README</router-link>

      <!-- 검색창: 관리자는 숨김 -->
      <input
        v-if="!authStore.isAdmin"
        class="search-input"
        type="text"
        placeholder="도서 검색..."
      />

      <!-- 헤더 우측 메뉴 -->
      <nav class="header-nav">

        <!-- 비로그인 -->
        <template v-if="!authStore.isLoggedIn">
          <router-link to="/signin">로그인</router-link>
          <router-link to="/signup">회원가입</router-link>
        </template>

        <!-- 일반 유저 로그인 상태 -->
        <template v-else-if="!authStore.isAdmin">
          <router-link to="/mypage/edit">회원 정보 수정</router-link>
          <router-link to="/mypage" class="icon-btn" title="마이페이지">👤 마이페이지</router-link>
          <router-link to="/cart" class="icon-btn" title="장바구니">🛒 장바구니</router-link>
          <button class="btn-logout" @click="handleLogout">로그아웃</button>
        </template>

        <!-- 관리자 로그인 상태 -->
        <template v-else>
          <input class="admin-search" type="text" placeholder="회원 검색..." />
          <router-link to="/admin/order/approval" class="icon-btn" title="주문 승인 요청">📋 주문 승인</router-link>
          <router-link to="/admin/product/stock" class="icon-btn" title="재고 목록">📦 재고 목록</router-link>
          <button class="btn-logout" @click="handleLogout">로그아웃</button>
        </template>

      </nav>
    </header>

    <!-- ===== 바디: 사이드바 + 메인 ===== -->
    <div class="app-body">

      <!-- 사이드바: signin/signup 페이지에서는 숨김 -->
      <aside class="sidebar" v-if="showSidebar">

        <!-- 일반 유저 사이드바 -->
        <template v-if="!authStore.isAdmin">

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('order')">
              주문/배송 <span>{{ open.order ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.order">
              <li><router-link to="/mypage/order">주문 목록</router-link></li>
              <li><router-link to="/cart">장바구니</router-link></li>
              <li><router-link to="/mypage/payment">결제 내역</router-link></li>
            </ul>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('board')">
              게시글 <span>{{ open.board ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.board">
              <li><router-link to="/notice">공지사항</router-link></li>
              <li><router-link to="/qna">QnA</router-link></li>
              <li><router-link to="/review">리뷰</router-link></li>
            </ul>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('myinfo')">
              회원 정보 <span>{{ open.myinfo ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.myinfo">
              <li><router-link to="/mypage/edit">정보 수정</router-link></li>
              <li><router-link to="/mypage/password">비밀번호 변경</router-link></li>
              <li><router-link to="/mypage/withdraw">회원 탈퇴</router-link></li>
            </ul>
          </div>

        </template>

        <!-- 관리자 사이드바 -->
        <template v-else>

          <div class="sidebar-section">
            <div class="sidebar-title">
              <router-link to="/admin">대시보드</router-link>
            </div>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('adminOrder')">
              주문 관리 <span>{{ open.adminOrder ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.adminOrder">
              <li><router-link to="/admin/order/list">주문 목록</router-link></li>
              <li><router-link to="/admin/delivery/list">배송 목록</router-link></li>
              <li><router-link to="/admin/category/list">카테고리 목록</router-link></li>
              <li><router-link to="/admin/payment/list">결제 목록</router-link></li>
            </ul>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('adminProduct')">
              상품 관리 <span>{{ open.adminProduct ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.adminProduct">
              <li><router-link to="/admin/product/list">상품 목록</router-link></li>
              <li><router-link to="/admin/product/insert">상품 추가</router-link></li>
            </ul>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('adminBoard')">
              게시글 <span>{{ open.adminBoard ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.adminBoard">
              <li><router-link to="/admin/notice/list">공지사항</router-link></li>
              <li><router-link to="/admin/qna/list">QnA 목록</router-link></li>
              <li><router-link to="/admin/review/list">리뷰 목록</router-link></li>
            </ul>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title" @click="toggle('adminMember')">
              회원 관리 <span>{{ open.adminMember ? '▾' : '▸' }}</span>
            </div>
            <ul v-show="open.adminMember">
              <li><router-link to="/admin/member/list">회원 목록</router-link></li>
              <li><router-link to="/admin/member/role">관리자 관리</router-link></li>
            </ul>
          </div>

        </template>
      </aside>

      <!-- 메인 콘텐츠 -->
      <main class="main-content">
        <router-view />
      </main>

    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

// 사이드바 숨김 경로 (로그인·회원가입 페이지는 전체 화면으로 사용)
const AUTH_PATHS = ['/signin', '/signup', '/oauth/callback'];
const showSidebar = computed(() => !AUTH_PATHS.includes(route.path));

// 사이드바 섹션 열림/닫힘 상태
const open = reactive({
  order:        true,
  board:        false,
  myinfo:       false,
  adminOrder:   true,
  adminProduct: false,
  adminBoard:   false,
  adminMember:  false,
});

function toggle(key) {
  open[key] = !open[key];
}

async function handleLogout() {
  await authStore.signout();
  alert('로그아웃 되었습니다.');
  router.push('/');
}
</script>

<style>
/* 전체 레이아웃 리셋 */
* { box-sizing: border-box; }

body { margin: 0; }

#app {
  width: 100%;
  min-height: 100svh;
  display: flex;
  flex-direction: column;
}

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

/* 대시보드처럼 하위 메뉴 없이 링크만 있을 때 */
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
