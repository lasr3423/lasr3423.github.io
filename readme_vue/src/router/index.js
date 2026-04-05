import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [

    // ── 레이아웃 없는 페이지 (로그인, 회원가입 등) ──────────────────
    { path: '/signin',        component: () => import('@/views/member/SigninView.vue') },
    { path: '/signup',        component: () => import('@/views/member/SignupView.vue') },
    { path: '/oauth/callback',component: () => import('@/views/member/OAuthCallbackView.vue') },

    // ── MainLayout 적용 페이지 ──────────────────────────────────────
    // component: MainLayout 을 부모로 두고, children 안에 각 뷰를 넣음
    // MainLayout 안의 <slot>에 children 컴포넌트가 들어감
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '',             component: () => import('@/views/Home.vue') },
        { path: 'product',      component: () => import('@/views/product/ProductListView.vue') },
        { path: 'product/:productId', component: () => import('@/views/product/ProductDetailView.vue') },
        {
          path: 'cart',
          component: () => import('@/views/cart/CartView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'mypage',
          component: () => import('@/views/member/MyPageView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'mypage/order',
          component: () => import('@/views/member/MyOrderView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'mypage/order/:orderId',
          component: () => import('@/views/member/MyOrderDetailView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'order',
          component: () => import('@/views/order/OrderView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'admin',
          component: () => import('@/views/admin/DashboardView.vue'),
          meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] }
        },
        { path: 'payment',         component: () => import('@/views/payment/PaymentView.vue') },
        { path: 'payment/success', component: () => import('@/views/payment/PaymentSuccessView.vue') },
        { path: 'payment/fail',    component: () => import('@/views/payment/PaymentFailView.vue') },
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  if (!authStore.accessToken) {
    await authStore.initialize()
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  }
  next()
})

export default router