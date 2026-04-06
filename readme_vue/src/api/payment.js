import api from '@/api/axios'

export const paymentApi = {

  // 카카오/네이버 결제 준비: tid + 결제창 URL 발급
  ready: (data) => api.post('/api/order/payment/ready', data),

  // 토스 결제 최종 승인: paymentKey, orderId, amount 전송
  confirm: (data) => api.post('/api/order/payment/confirm', data),

  // 카카오/네이버 결제 최종 승인
  approve: (data) => api.post('/api/order/payment/approve', data),

  // 토스 결제 실패 처리
  fail: (data) => api.post('/api/order/payment/fail', data),

}
