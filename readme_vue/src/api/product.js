import api from '@/api/axios'

export const productApi = {

  // 상품 목록 조회 (페이징, 카테고리/키워드 필터)
  getList: (params) => api.get('/api/product', { params }),

  // 상품 상세 조회
  getDetail: (productId) => api.get(`/api/product/detail/${productId}`),

}
