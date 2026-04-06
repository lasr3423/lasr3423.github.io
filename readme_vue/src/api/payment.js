import api from '@/api/axios'

// paymentApi 객체로 묶어서 export — auth.js와 동일한 패턴
export const paymentApi = {

  // 카카오/네이버 결제 준비: tid + 결제창 URL 발급
  // 호출: paymentApi.ready({ orderId, provider, itemName, amount, approvalUrl, cancelUrl, failUrl })
  ready: (data) => api.post('/api/order/payment/ready', data),

  // 토스 결제 최종 승인: paymentKey, orderId, amount 전송
  // 호출: paymentApi.confirm({ paymentKey, orderId, amount })
  confirm: (data) => api.post('/api/order/payment/confirm', data),

  // 카카오/네이버 결제 최종 승인
  // 호출: paymentApi.approve({ orderId, provider, tid, pgToken, paymentId })
  approve: (data) => api.post('/api/order/payment/approve', data),

  // 토스 결제 실패 처리
  // 호출: paymentApi.fail({ orderId, code, message })
  fail: (data) => api.post('/api/order/payment/fail', data),
}