import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/Home.vue') },
    { path: '/signin', component: () => import('@/views/member/SigninView.vue') },
    { path: '/signup', component: () => import('@/views/member/SignupView.vue') },
    { path: '/oauth/callback', component: () => import('@/views/member/OAuthCallbackView.vue') },
    {
       path: '/mypage', component: () => import('@/views/member/MyPageView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin', component: () => import('@/views/admin/DashboardView.vue'),
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] }
    }
  ]
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  if (!authStore.accessToken) {
    await authStore.initialize();   // RefreshToken으로 상태 복구 시도
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  };

  next();
})

export default router;