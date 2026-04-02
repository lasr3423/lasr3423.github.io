// ─── router/index.js 에 추가할 QnA 라우트 ───────────────────────────
// 기존 createRouter({ ... routes: [ ... ] }) 배열에 아래 항목을 추가하세요.

import QnaListView        from '@/views/qna/QnaListView.vue'
import QnaWriteView       from '@/views/qna/QnaWriteView.vue'
import QnaDetailView      from '@/views/qna/QnaDetailView.vue'
import AdminQnaListView   from '@/views/admin/AdminQnaListView.vue'
import AdminQnaDetailView from '@/views/admin/AdminQnaDetailView.vue'

export const qnaRoutes = [
  // ── 사용자 ──────────────────────────────────────────────────
  {
    path: '/qna/list',
    name: 'QnaList',
    component: QnaListView,
    meta: { title: '1:1 문의' },
  },
  {
    path: '/qna/write',
    name: 'QnaWrite',
    component: QnaWriteView,
    meta: { title: '문의 작성', requiresAuth: true },
  },
  {
    path: '/qna/detail/:id',
    name: 'QnaDetail',
    component: QnaDetailView,
    meta: { title: 'QnA 상세' },
  },

  // ── 관리자 ──────────────────────────────────────────────────
  {
    path: '/admin/qna/list',
    name: 'AdminQnaList',
    component: AdminQnaListView,
    meta: { title: 'QnA 관리', requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/qna/detail/:id',
    name: 'AdminQnaDetail',
    component: AdminQnaDetailView,
    meta: { title: 'QnA 상세·답변', requiresAuth: true, requiresAdmin: true },
  },
]

// ─── 라우터 네비게이션 가드 예시 (router/index.js 의 beforeEach) ───
/*
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.accessToken) {
    return next({ name: 'Signin', query: { redirect: to.fullPath } })
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return next({ name: 'Home' })
  }
  next()
})
*/