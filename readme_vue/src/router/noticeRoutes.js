// ──────────────────────────────────────────────
// router/index.js 에 아래 routes 배열을 추가하세요
// ──────────────────────────────────────────────

// 📢 공지사항 — 사용자 라우트
const noticeUserRoutes = [
  {
    path: '/notice/list',
    name: 'NoticeList',
    component: () => import('@/views/notice/NoticeListView.vue'),
    meta: { title: '공지사항' },
  },
  {
    path: '/notice/detail/:id',
    name: 'NoticeDetail',
    component: () => import('@/views/notice/NoticeDetailView.vue'),
    meta: { title: '공지사항 상세' },
  },
]

// 🔐 공지사항 — 관리자 라우트 (MANAGER/ADMIN 권한 필요)
const noticeAdminRoutes = [
  {
    path: '/admin/notice/list',
    name: 'AdminNoticeList',
    component: () => import('@/views/admin/AdminNoticeListView.vue'),
    meta: { title: '공지사항 관리', requiresAdmin: true },
  },
  {
    path: '/admin/notice/detail/:id',
    name: 'AdminNoticeDetail',
    component: () => import('@/views/admin/AdminNoticeDetailView.vue'),
    meta: { title: '공지사항 상세', requiresAdmin: true },
  },
  {
    path: '/admin/notice/insert',
    name: 'AdminNoticeInsert',
    component: () => import('@/views/admin/AdminNoticeFormView.vue'),
    meta: { title: '공지사항 등록', requiresAdmin: true },
  },
  {
    path: '/admin/notice/update/:id',
    name: 'AdminNoticeUpdate',
    component: () => import('@/views/admin/AdminNoticeFormView.vue'),
    meta: { title: '공지사항 수정', requiresAdmin: true },
  },
]

export { noticeUserRoutes, noticeAdminRoutes }

// ──────────────────────────────────────────────
// 사용 예시 (router/index.js)
// ──────────────────────────────────────────────
//
// import { noticeUserRoutes, noticeAdminRoutes } from './noticeRoutes'
//
// const routes = [
//   ...noticeUserRoutes,
//
//   {
//     path: '/admin',
//     component: AdminLayout,
//     children: [
//       ...noticeAdminRoutes,
//     ],
//   },
// ]