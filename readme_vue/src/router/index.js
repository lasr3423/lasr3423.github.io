import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [

    // ────────────────────────────────────────────────────────
    // [ Main 그룹 ]  메인 / 상품 / 장바구니 / 주문 / 결제
    //  └ layouts/MainLayout.vue (AppHeader + AppSidebar + AppFooter)
    // ────────────────────────────────────────────────────────
    // ※ 반드시 Auth 그룹보다 먼저 위치해야 함!
    //   두 그룹 모두 path:'/'를 부모로 가지므로, Vue Router는 첫 번째로
    //   완전히 매칭되는 라우트를 사용함. Main 그룹의 path:'' 자식이
    //   '/' 를 완전히 소비하므로 Home.vue가 올바르게 렌더링됨.
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
          path: 'notice',
          component: () => import('@/views/board/NoticeListView.vue')
        },
        {
          path: 'qna',
          component: () => import('@/views/board/QnaListView.vue')
        },
        {
          path: 'review',
          component: () => import('@/views/board/ReviewListView.vue')
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
          component: () => import('@/views/payment/PaymentView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'payment/success',
          component: () => import('@/views/payment/PaymentSuccessView.vue')
        },
        {
          path: 'payment/fail',
          component: () => import('@/views/payment/PaymentFailView.vue')
        },
        {
          path: 'payment/complete',
          component: () => import('@/views/payment/PaymentCompleteView.vue')
        },
      ]
    },

    // ────────────────────────────────────────────────────────
    // [ Auth 그룹 ]  로그인 / 회원가입 / OAuth 콜백
    //  └ layouts/AuthLayout.vue (GuestHeader, 사이드바 없음)
    // ────────────────────────────────────────────────────────
    // ※ Main 그룹 이후에 위치 — '/'는 이미 Main 그룹이 선점하므로
    //   '/signin', '/signup' 등 하위 경로만 이 그룹에서 처리됨.
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
        {
          path: 'payment',
          component: () => import('@/views/member/MyPaymentListView.vue')
        },
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
        {
          path: 'qna',
          component: () => import('@/views/member/MyQnaView.vue')
        },
        {
          path: 'qna/detail/:id',
          component: () => import('@/views/member/MyQnaDetailView.vue')
        },
        {
          path: 'review',
          component: () => import('@/views/member/MyReviewView.vue')
        },
        {
          path: 'review/detail/:id',
          component: () => import('@/views/member/MyReviewDetailView.vue')
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
        { path: 'order/approval', component: () => import('@/views/admin/order/OrderApprovalView.vue') },
        { path: 'delivery/list',  component: () => import('@/views/admin/delivery/DeliveryListView.vue') },
        { path: 'category/list',  component: () => import('@/views/admin/category/CategoryListView.vue') },
        { path: 'payment/list',   component: () => import('@/views/admin/payment/PaymentListView.vue') },
        // 상품 관리
        { path: 'product/list',   component: () => import('@/views/admin/product/ProductListView.vue') },
        { path: 'product/stock',  component: () => import('@/views/admin/product/ProductStockView.vue') },
        { path: 'product/insert', component: () => import('@/views/admin/product/ProductInsertView.vue') },
        // 게시글 관리
        { path: 'notice/list',    component: () => import('@/views/admin/notice/NoticeListView.vue') },
        { path: 'qna/list',       component: () => import('@/views/admin/qna/QnaListView.vue') },
        { path: 'review/list',    component: () => import('@/views/admin/review/ReviewListView.vue') },
        // 회원 관리
        { path: 'member/list',    component: () => import('@/views/admin/member/MemberListView.vue') },
        { path: 'member/role',    component: () => import('@/views/admin/member/MemberRoleView.vue') },
      ]
    },

    // ── 404 (최상위에 위치해야 함) ─────────────────────────────────
    {
      path: '/:pathMatch(.*)*',
      component: () => import('@/views/NotFoundView.vue'),
    },

  ]
})

// ── 라우터 가드 (인증 체크) ────────────────────────────────────────
// Vue Router 4: next() 대신 return 값으로 처리
router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authStore.accessToken) {
    await authStore.initialize()
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { path: '/signin', query: { redirect: to.fullPath } }
  }
})

export default router;
