import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const Todo = {
  template: `
    <div class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center text-sm text-slate-500">
      This page is still being prepared.
    </div>
  `,
};

const adminMeta = (extra = {}) => ({
  requiresAuth: true, roles: ['MANAGER', 'ADMIN'], layout: 'admin', ...extra,
});

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ── 공개 페이지 ────────────────────────────────────────────────────────
    { path: '/',                    component: () => import('@/views/Home.vue') },
    { path: '/product',             component: () => import('@/views/product/ProductListView.vue') },
    { path: '/product/:productId',  component: () => import('@/views/product/ProductDetailView.vue') },
    { path: '/notice',              component: Todo },
    { path: '/qna',                 component: Todo },
    { path: '/review',              component: Todo },

    // ── 인증 페이지 ────────────────────────────────────────────────────────
    { path: '/signin',         component: () => import('@/views/member/SigninView.vue'),       meta: { layout: 'auth' } },
    { path: '/signup',         component: () => import('@/views/member/SignupView.vue'),       meta: { layout: 'auth' } },
    { path: '/oauth/callback', component: () => import('@/views/member/OAuthCallbackView.vue'), meta: { layout: 'auth' } },

    // ── 결제 ───────────────────────────────────────────────────────────────
    { path: '/payment',         component: () => import('@/views/payment/PaymentView.vue'),        meta: { requiresAuth: true } },
    { path: '/payment/success', component: () => import('@/views/payment/PaymentSuccessView.vue'), meta: { requiresAuth: true } },
    { path: '/payment/fail',    component: () => import('@/views/payment/PaymentFailView.vue') },

    // ── 마이페이지 ─────────────────────────────────────────────────────────
    { path: '/mypage',               component: () => import('@/views/member/MyPageView.vue'),       meta: { requiresAuth: true } },
    { path: '/mypage/edit',          component: () => import('@/views/member/MyPageEditView.vue'),   meta: { requiresAuth: true } },
    { path: '/mypage/password',      component: () => import('@/views/member/MyPasswordView.vue'),   meta: { requiresAuth: true } },
    { path: '/mypage/withdraw',      component: () => import('@/views/member/MyWithdrawView.vue'),   meta: { requiresAuth: true } },
    { path: '/mypage/order',         component: () => import('@/views/member/MyOrderView.vue'),      meta: { requiresAuth: true } },
    { path: '/mypage/order/:orderId',component: () => import('@/views/member/MyOrderDetailView.vue'),meta: { requiresAuth: true } },
    { path: '/mypage/payment',       component: Todo,                                               meta: { requiresAuth: true } },

    // ── 장바구니 / 주문 ────────────────────────────────────────────────────
    { path: '/cart',  component: () => import('@/views/cart/CartView.vue'),  meta: { requiresAuth: true } },
    { path: '/order', component: () => import('@/views/order/OrderView.vue'), meta: { requiresAuth: true } },

    // ── 관리자 ─────────────────────────────────────────────────────────────
    { path: '/admin',                 component: () => import('@/views/admin/DashboardView.vue'),              meta: adminMeta() },
    { path: '/admin/member/list',     component: () => import('@/views/admin/member/MemberListView.vue'),      meta: adminMeta() },
    { path: '/admin/member/role',     component: () => import('@/views/admin/member/MemberListView.vue'),      meta: adminMeta() },
    { path: '/admin/product/list',    component: () => import('@/views/admin/product/ProductListView.vue'),    meta: adminMeta() },
    { path: '/admin/product/insert',  component: () => import('@/views/admin/product/ProductInsertView.vue'),  meta: adminMeta() },
    { path: '/admin/product/stock',   component: () => import('@/views/admin/product/ProductListView.vue'),    meta: adminMeta() },
    { path: '/admin/order/list',      component: () => import('@/views/admin/order/OrderListView.vue'),        meta: adminMeta() },
    { path: '/admin/order/approval',  component: () => import('@/views/admin/order/OrderListView.vue'),        meta: adminMeta() },
    { path: '/admin/delivery/list',   component: () => import('@/views/admin/delivery/DeliveryListView.vue'),  meta: adminMeta() },
    { path: '/admin/category/list',   component: Todo,                                                        meta: adminMeta() },
    { path: '/admin/payment/list',    component: Todo,                                                        meta: adminMeta() },
    { path: '/admin/notice/list',     component: Todo,                                                        meta: adminMeta() },
    { path: '/admin/qna/list',        component: Todo,                                                        meta: adminMeta() },
    { path: '/admin/review/list',     component: Todo,                                                        meta: adminMeta() },

    // ── 404 ────────────────────────────────────────────────────────────────
    {
      path: '/:pathMatch(.*)*',
      component: {
        template: `
          <div class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center text-sm text-slate-500">
            The page you are looking for does not exist.
          </div>
        `,
      },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (!authStore.accessToken) {
    await authStore.initialize();
  }

  const guestOnly = ['/signin', '/signup'];
  if (guestOnly.includes(to.path) && authStore.isLoggedIn) {
    return next({ path: '/' });
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } });
  }

  if (to.meta.roles && !to.meta.roles.includes(authStore.userRole)) {
    return next({ path: '/' });
  }

  next();
});

export default router;
