import axios from '@/api/axios.js'
 
// ── 사용자 ──────────────────────────────────────────────
export const getNoticeList = (page = 0) =>
  axios.get('/notice/list', { params: { page } })
 
export const getNoticeDetail = (id) =>
  axios.get(`/notice/detail/${id}`)
 
// ── 관리자 ──────────────────────────────────────────────
export const getAdminNoticeList = (page = 0) =>
  axios.get('/admin/notice/list', { params: { page } })
 
export const getAdminNoticeDetail = (id) =>
  axios.get(`/admin/notice/detail/${id}`)
 
export const createNotice = (data) =>
  axios.post('/admin/notice/insert', data)
 
export const updateNotice = (id, data) =>
  axios.patch(`/admin/notice/update/${id}`, data)
 
export const deleteNotice = (id) =>
  axios.delete(`/admin/notice/${id}`)
 