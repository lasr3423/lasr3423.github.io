/**
 * QnA 도메인 라우트 설정
 * 연관 요구사항: REQ-Q-001 ~ REQ-Q-006, REQ-M-021 ~ 024
 */
export const qnaRoutes = [
  {
    path: 'qna',
    name: 'QnaList',
    component: () => import('@/views/qna/QnaListView.vue'),
    meta: { title: '1:1 문의 목록' }
  },
  {
    path: 'qna/write',
    name: 'QnaWrite',
    component: () => import('@/views/qna/QnaWriteView.vue'),
    meta: { title: '문의 등록', requiresAuth: true }
  },
  {
    path: 'qna/detail/:id',
    name: 'QnaDetail',
    component: () => import('@/views/qna/QnaDetailView.vue'),
    meta: { title: '문의 상세 내용' } // 계층 구조 표시 [cite: 1380]
  },
  {
    path: 'qna/edit/:id',
    name: 'QnaEdit',
    component: () => import('@/views/qna/QnaEditView.vue'),
    meta: { title: '문의 수정', requiresAuth: true } // 답변 완료 후 수정 불가 정책 반영 [cite: 1290]
  },
  {
    path: 'qna/delete-success',
    name: 'QnaDeleteSuccess',
    component: () => import('@/views/qna/QnaDeleteSuccessView.vue'),
    meta: { title: '삭제 완료' }
  }
];