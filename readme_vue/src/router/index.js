import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const Todo = { template: '<div style="padding:20px;color:#888;"><h3>🚧 준비 중인 페이지입니다.</h3></div>' };

const router = createRouter({
  history: createWebHistory(),
  routes: [

    // ────────────────────────────────────────────────────────
    // [ Auth 그룹 ]  로그인 / 회원가입 / OAuth 콜백
    //  └ layouts/AuthLayout.vue (AppHeader + AppFooter, 사이드바 없음)
    // ────────────────────────────────────────────────────────
    {
      path: '/',
      component: () => import('@/layouts/AuthLayout.vue'),
      children: [
        {
          path: 'signin',
          component: () => import('@/views/member/SigninView.vue')
        },
        {
          path: 'signup',
          component: () => import('@/views/member/SignupView.vue')
        },
        {
          path: 'oauth/callback',
          component: () => import('@/views/member/OAuthCallbackView.vue')
        },
      ]
    },

    // ────────────────────────────────────────────────────────
    // [ Main 그룹 ]  메인 / 상품 / 장바구니 / 주문 / 결제
    //  └ layouts/MainLayout.vue (AppHeader + AppSidebar + AppFooter)
    // ────────────────────────────────────────────────────────
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: '',
          component: () => import('@/views/Home.vue')
        },
        {
          path: 'product',
          component: () => import('@/views/product/ProductListView.vue')
        },
        {
          path: 'product/:productId',
          component: () => import('@/views/product/ProductDetailView.vue')
        },
        {
          path: 'cart',
          component: () => import('@/views/cart/CartView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'order',
          component: () => import('@/views/order/OrderView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'payment',
          component: () => import('@/views/payment/PaymentView.vue')
        },
        {
          path: 'payment/success',
          component: () => import('@/views/payment/PaymentSuccessView.vue')
        },
        {
          path: 'payment/fail',
          component: () => import('@/views/payment/PaymentFailView.vue')
        },
      ]
    },

    // ────────────────────────────────────────────────────────
    // [ MyPage 그룹 ]  마이페이지 (로그인 필수)
    //  └ layouts/MypageLayout.vue (MyHeader + MySidebar + MyFooter)
    // ────────────────────────────────────────────────────────
    {
      path: '/mypage',
      component: () => import('@/layouts/MypageLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          component: () => import('@/views/member/MyPageView.vue')
        },
        {
          path: 'order',
          component: () => import('@/views/member/MyOrderView.vue')
        },
        {
          path: 'order/:orderId',
          component: () => import('@/views/member/MyOrderDetailView.vue')
        },
        // {
        //   path: 'payment',
        //   component: () => import('@/views/member/MyPaymentList.vue')
        // },
        {
          path: 'edit',
          component: () => import('@/views/member/MyPageEditView.vue')
        },
        {
          path: 'password',
          component: () => import('@/views/member/MyPasswordView.vue')
        },
        {
          path: 'withdraw',
          component: () => import('@/views/member/MyWithdrawView.vue')
        },
      ]
    },

    // ────────────────────────────────────────────────────────
    // [ Admin 그룹 ]  관리자 페이지 (MANAGER / ADMIN 권한 필수)
    //  └ layouts/AdminLayout.vue (AdminHeader + AdminSidebar)
    // ────────────────────────────────────────────────────────
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] },
      children: [
        {
          path: '',
          component: () => import('@/views/admin/DashboardView.vue')
        },
        // 주문/배송 관리
        { path: 'order/list',     component: () => import('@/views/admin/order/OrderListView.vue') },
        { path: 'order/approval', component: Todo },
        { path: 'delivery/list',  component: () => import('@/views/admin/delivery/DeliveryListView.vue') },
        { path: 'category/list',  component: Todo },
        { path: 'payment/list',   component: Todo },
        // 상품 관리
        { path: 'product/list',   component: () => import('@/views/admin/product/ProductListView.vue') },
        { path: 'product/stock',  component: Todo },
        { path: 'product/insert', component: () => import('@/views/admin/product/ProductInsertView.vue') },
        // 게시글 관리
        { path: 'notice/list',    component: Todo },
        { path: 'qna/list',       component: Todo },
        { path: 'review/list',    component: Todo },
        // 회원 관리
        { path: 'member/list',    component: () => import('@/views/admin/member/MemberListView.vue') },
        { path: 'member/role',    component: Todo },
      ]
    },

    // ── 404 (최상위에 위치해야 함) ─────────────────────────────────
    {
      path: '/:pathMatch(.*)*',
      component: { template: '<div style="padding:40px;text-align:center;"><h3>404 — 페이지를 찾을 수 없습니다.</h3><router-link to="/">홈으로</router-link></div>' },
    },

  ]
})

// ── 라우터 가드 (인증 체크) ────────────────────────────────────────
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (!authStore.accessToken) {
    await authStore.initialize()
  }

  // requiresAuth: true 인 route에 미로그인 접근 시 로그인 페이지로
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  }

  next()
})

export default router;