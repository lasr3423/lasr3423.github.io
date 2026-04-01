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
    },
    // 결제 관련 라우터 추가, 각 URL은 PG사 콘솔에 등록한 successUrl / failUrl / approval_url / returnUrl과 일치해야 함
    { path: '/payment', component: () => import('@/views/payment/PaymentView.vue')},
    {
      path: '/payment/success',
      component: () => import('@/views/payment/PaymentSuccessView.vue'),  // 토스 successUrl 처리
    },
    {
      path: '/payment/fail',
      component: () => import('@/views/payment/PaymentFailView.vue'),     // 토스 failUrl 처리
    },
    // {
    //   path: '/payment/kakao/success',
    //   component: () => import('@/views/payment/KakaoSuccessView.vue'),   // 카카오 approval_url 처리
    // },
    // {
    //   path: '/payment/naver/success',
    //   component: () => import('@/views/payment/NaverSuccessView.vue'),   // 네이버 returnUrl 처리
    // }
    {
      // 마이페이지 주문 목록
      // PaymentSuccessView에서 결제 완료 후 이 경로로 이동
      path: '/mypage/order',
      component: () => import('@/views/member/MyOrderView.vue'),
      meta: { requiresAuth: true }  // 로그인 필수
    },
    {
      // 주문 상세 — 특정 orderId의 상세 정보
      path: '/mypage/order/:orderId',
      component: () => import('@/views/member/MyOrderDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      // 장바구니 → 주문서 작성
      // 체크아웃 버튼 클릭 시 이동, OrderStore.setOrder() 호출 후 PaymentView로 이동
      path: '/order',
      component: () => import('@/views/order/OrderView.vue'),
      meta: { requiresAuth: true }
    },
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