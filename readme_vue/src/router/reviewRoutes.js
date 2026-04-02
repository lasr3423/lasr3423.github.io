// ─────────────────────────────────────────────────
// router/index.js 에 추가할 리뷰 라우트 정의
// ─────────────────────────────────────────────────

import ReviewWriteView  from '@/views/review/ReviewWriteView.vue'
import ReviewDetailView from '@/views/review/ReviewDetailView.vue'
import AdminReviewView  from '@/views/admin/AdminReviewView.vue'

export const reviewRoutes = [
  // FR-002, FR-003  리뷰 작성
  // query: { productId }  필수
  {
    path: '/review/write',
    name: 'ReviewWrite',
    component: ReviewWriteView,
    meta: { requiresAuth: true }   // JWT 인증 필수
  },

  // FR-005  리뷰 상세 조회 (view_count +1)
  {
    path: '/review/detail/:id',
    name: 'ReviewDetail',
    component: ReviewDetailView
  },

  // FA-029~031  관리자 리뷰 관리
  {
    path: '/admin/review',
    name: 'AdminReview',
    component: AdminReviewView,
    meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] }
  }
]

// ─────────────────────────────────────────────────
// 기존 index.js 의 createRouter() 에 spread 삽입 예시
// ─────────────────────────────────────────────────
//
// import { reviewRoutes } from './reviewRoutes.js'
//
// const router = createRouter({
//   history: createWebHistory(),
//   routes: [
//     ...기존라우트,
//     ...reviewRoutes
//   ]
// })