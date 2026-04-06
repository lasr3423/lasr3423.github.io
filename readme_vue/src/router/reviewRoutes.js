/**
 * 리뷰 도메인 라우트 설정
 * 연관 요구사항: REQ-R-001 ~ REQ-R-005, REQ-M-016 ~ 020
 */
export const reviewRoutes = [
  {
    path: 'review/list',
    name: 'ReviewList',
    component: () => import('@/views/review/ReviewListView.vue'),
    meta: { title: '상품 리뷰 목록' }
  },
  {
    path: 'review/write',
    name: 'ReviewWrite',
    component: () => import('@/views/review/ReviewWriteView.vue'),
    meta: { title: '리뷰 작성', requiresAuth: true } // 구매 확정 유저만 접근 [cite: 1385]
  },
  {
    path: 'review/detail/:id',
    name: 'ReviewDetail',
    component: () => import('@/views/review/ReviewDetailView.vue'),
    meta: { title: '리뷰 상세 조회' } // 조회수 자동 증가 로직 포함 [cite: 1389]
  },
  {
    path: 'review/edit/:id',
    name: 'ReviewEdit',
    component: () => import('@/views/review/ReviewEditView.vue'),
    meta: { title: '리뷰 수정', requiresAuth: true }
  },
  {
    path: 'review/delete-success',
    name: 'ReviewDeleteSuccess',
    component: () => import('@/views/review/ReviewDeleteSuccessView.vue'),
    meta: { title: '삭제 완료' }
  }
];