import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/store/auth';

// 아직 구현 전인 페이지에 임시로 사용하는 컴포넌트
const Todo = { template: '<div style="padding:20px;color:#888;"><h3>🚧 준비 중인 페이지입니다.</h3></div>' };

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ── 공통 ──────────────────────────────────────────────────────
    { path: '/',               component: () => import('@/views/Home.vue') },
    { path: '/signin',         component: () => import('@/views/member/SigninView.vue'),          meta: { layout: 'auth' } },
    { path: '/signup',         component: () => import('@/views/member/SignupView.vue'),           meta: { layout: 'auth' } },
    { path: '/oauth/callback', component: () => import('@/views/member/OAuthCallbackView.vue'),   meta: { layout: 'auth' } },

    // ── 결제 ──────────────────────────────────────────────────────
    { path: '/payment',         component: () => import('@/views/payment/PaymentView.vue'),        meta: { requiresAuth: true } },
    { path: '/payment/success', component: () => import('@/views/payment/PaymentSuccessView.vue'), meta: { requiresAuth: true } },
    { path: '/payment/fail',    component: () => import('@/views/payment/PaymentFailView.vue') },

    // ── 마이페이지 (일반 유저) ─────────────────────────────────────
    {
      path: '/mypage',
      component: () => import('@/views/member/MyPageView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mypage/order',
      component: () => import('@/views/member/MyOrderView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mypage/order/:orderId',
      component: () => import('@/views/member/MyOrderDetailView.vue'),
      meta: { requiresAuth: true },
    },
    { path: '/mypage/payment',  component: Todo, meta: { requiresAuth: true } },
    { path: '/mypage/edit',     component: Todo, meta: { requiresAuth: true } },
    { path: '/mypage/password', component: Todo, meta: { requiresAuth: true } },
    { path: '/mypage/withdraw', component: Todo, meta: { requiresAuth: true } },

    // ── 장바구니 / 주문 ────────────────────────────────────────────
    { path: '/cart',  component: Todo, meta: { requiresAuth: true } },
    {
      path: '/order',
      component: () => import('@/views/order/OrderView.vue'),
      meta: { requiresAuth: true },
    },

    // ── 게시글 (비회원 접근 가능) ──────────────────────────────────
    { path: '/notice', component: Todo },
    { path: '/qna',    component: Todo },
    { path: '/review', component: Todo },

    // ── 관리자 ────────────────────────────────────────────────────
    {
      path: '/admin',
      component: () => import('@/views/admin/DashboardView.vue'),
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' },
    },
    // 주문 관리
    { path: '/admin/order/list',      component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/order/approval',  component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/delivery/list',   component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/category/list',   component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/payment/list',    component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    // 상품 관리
    { path: '/admin/product/list',    component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/product/stock',   component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/product/insert',  component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    // 게시글 관리
    { path: '/admin/notice/list',     component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/qna/list',        component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/review/list',     component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    // 회원 관리
    { path: '/admin/member/list',     component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },
    { path: '/admin/member/role',     component: Todo, meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin' } },

    // ── 404 ───────────────────────────────────────────────────────
    {
      path: '/:pathMatch(.*)*',
      component: { template: '<div style="padding:20px;"><h3>404 — 페이지를 찾을 수 없습니다.</h3><router-link to="/">홈으로</router-link></div>' },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // 앱 첫 진입 시 RefreshToken으로 로그인 상태 복구
  if (!authStore.accessToken) {
    await authStore.initialize();
  }

  // 이미 로그인한 상태에서 로그인/회원가입 페이지 접근 시 홈으로
  const GUEST_ONLY = ['/signin', '/signup'];
  if (GUEST_ONLY.includes(to.path) && authStore.isLoggedIn) {
    return next({ path: '/' });
  }

  // 인증 필요한 페이지인데 비로그인이면 로그인 페이지로
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } });
  }

  // 역할 제한이 있는 페이지인데 권한 없으면 홈으로
  if (to.meta.roles && !to.meta.roles.includes(authStore.userRole)) {
    return next({ path: '/' });
  }

  next();
});

export default router;
