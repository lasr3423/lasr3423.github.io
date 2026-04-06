import api from '@/api/axios'

export const cartApi = {

  // GET - 내 장바구니 목록 가져오기
  getItems: () => api.get('/api/cart'),

  // POST - 장바구니에 상품 담기
  addItem: (productId, quantity) => api.post('/api/cart', { productId, quantity }),

  // PUT - 수량 변경
  updateItem: (cartItemId, quantity) => api.put(`/api/cart/${cartItemId}`, { quantity }),

  // DELETE - 장바구니 항목 삭제
  removeItem: (cartItemId) => api.delete(`/api/cart/${cartItemId}`),

}
