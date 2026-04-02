import api from './axios.js'

// ──────────────────────────────────────────────
// FR-001  상품별 리뷰 목록 조회 (평점 평균 포함)
// ──────────────────────────────────────────────
export const getReviewList = (productId, page = 0) =>
  api.get(`/product/category/detail/${productId}/reviews`, { params: { page } })

// ──────────────────────────────────────────────
// FR-005  리뷰 상세 조회 (view_count +1)
// ──────────────────────────────────────────────
export const getReviewDetail = (reviewId) =>
  api.get(`/review/detail/${reviewId}`)

// ──────────────────────────────────────────────
// FR-002 + FR-003  리뷰 작성 (이미지 포함 multipart)
// ──────────────────────────────────────────────
export const writeReview = (formData) =>
  api.post('/review/write', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

// ──────────────────────────────────────────────
// FR-004  리뷰 반응 (LIKE / DISLIKE)
// ──────────────────────────────────────────────
export const reactReview = (reviewId, reactionType) =>
  api.post(`/review/${reviewId}/reaction`, { reactionType })