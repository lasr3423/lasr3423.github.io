import axios from '@/api/axios.js'

// ─── 사용자 QnA API ────────────────────────────────────────────────

/**
 * QnA 목록 조회 (FQ-001)
 * GET /qna/list?page=0
 */
export const getQnaList = (page = 0) =>
  axios.get('/qna/list', { params: { page } })

/**
 * QnA 상세 조회 (FQ-006)
 * GET /qna/detail/{id}
 */
export const getQnaDetail = (id) =>
  axios.get(`/qna/detail/${id}`)

/**
 * QnA 질문 등록 (FQ-002)
 * POST /qna/write
 * body: { title, content, category, isSecret, productId? }
 */
export const writeQna = (payload) =>
  axios.post('/qna/write', payload)

/**
 * n차 재문의 등록 (FQ-004) — depth 최대 4
 * POST /qna/{id}
 * body: { content, parentId }
 */
export const writeReply = (parentId, payload) =>
  axios.post(`/qna/${parentId}`, payload)

// ─── 관리자 QnA API ────────────────────────────────────────────────

/**
 * 관리자 QnA 목록 조회 (FA-032)
 * GET /admin/qna/list?page=0&status=WAITING
 */
export const adminGetQnaList = (page = 0, status = '') =>
  axios.get('/admin/qna/list', { params: { page, ...(status && { status }) } })

/**
 * 관리자 QnA 상세 조회 (FA-033)
 * GET /admin/qna/detail/{id}
 */
export const adminGetQnaDetail = (id) =>
  axios.get(`/admin/qna/detail/${id}`)

/**
 * QnA 상태 변경 (FA-034)
 * PATCH /admin/qna/{id}/status
 * body: { qnaStatus }
 */
export const adminUpdateQnaStatus = (id, qnaStatus) =>
  axios.patch(`/admin/qna/${id}/status`, { qnaStatus })

/**
 * 답변 등록 (FA-035)
 * POST /admin/qna/{id}/insert
 * body: { content }
 */
export const adminAnswerQna = (id, content) =>
  axios.post(`/admin/qna/${id}/insert`, { content })

/**
 * 답변 수정 (FA-036)
 * PATCH /admin/qna/update/{id}
 * body: { content }
 */
export const adminUpdateAnswer = (id, content) =>
  axios.patch(`/admin/qna/update/${id}`, { content })

/**
 * 답변 삭제 (FA-037) — soft delete
 * DELETE /admin/qna/{id}
 */
export const adminDeleteQna = (id) =>
  axios.delete(`/admin/qna/${id}`)