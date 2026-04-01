import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/Home.vue') },
    { path: '/signin', component: () => import('@/views/member/SigninView.vue') },
    { path: '/signup', component: () => import('@/views/member/SignupView.vue') },
    { path: '/oauth/callback', component: () => import('@/views/member/OAuthCallbackView.vue') },
    
    // --- 공지사항 (Notice) [cite: 1101, 1103] ---
    { path: '/notice', component: () => import('@/views/notice/NoticeListView.vue') },
    { path: '/notice/:id', component: () => import('@/views/notice/NoticeDetailView.vue') },

    // --- 마이페이지 (인증 필요) [cite: 974] ---
    { 
      path: '/mypage', 
      component: () => import('@/views/member/MyPageView.vue'),
      meta: { requiresAuth: true }
    },

    // --- 관리자 (인증 + 권한 필요) [cite: 878, 1135] ---
    {
      path: '/admin',
      component: () => import('@/views/admin/DashboardView.vue'),
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] },
      children: [
        {
          path: 'notice',
          component: () => import('@/views/admin/AdminNoticeView.vue'),
          meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] }
        }
      ]
    },
  ]
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // 1. 페이지 이동 전 토큰이 있다면 상태 복구 시도 (v1.3 토큰 갱신 전략) [cite: 659, 1153]
  if (authStore.accessToken && !authStore.isLoggedIn) {
    await authStore.initialize();
  }

  // 2. 인증이 필요한 페이지 접근 제어 [cite: 350]
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } });
  }

  // 3. 관리자 권한(Role) 체크 로직 추가 [cite: 354, 407]
  if (to.meta.roles && !to.meta.roles.includes(authStore.userRole)) {
    alert("접근 권한이 없습니다.");
    return next({ path: '/' }); // 권한 없으면 홈으로 리다이렉트
  }

  next();
});

export default router;