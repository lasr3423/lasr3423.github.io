import api from '@/api/axios'

export const orderApi = {
  // 주문 생성 → POST /api/order
  // body: { cartItemIds, receiverName, receiverPhone, deliveryAddress, deliveryAddressDetail, deliveryZipCode, deliveryMemo }
  // 응답: { orderId, finalPrice, itemName } → orderStore.setOrder() 에 사용
  create: (data) => api.post('/api/order', data),

  // 주문 목록 조회 → GET /api/order?page=0&size=10&sort=orderAt,desc
  getList: (params) => api.get('/api/order', { params }),

  // 주문 상세 조회 → GET /api/order/{orderId}
  getDetail: (orderId) => api.get(`/api/order/${orderId}`),

  // 주문 취소 → DELETE /api/order/{orderId}  body: { cancelReason }
  cancel: (orderId, cancelReason) =>
    api.delete(`/api/order/${orderId}`, { data: { cancelReason } }),
}