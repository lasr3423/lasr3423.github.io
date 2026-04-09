import api from '@/api/axios'

export const paymentApi = {
  ready: (data) => api.post('/api/order/payment/ready', data),
  confirm: (data) => api.post('/api/order/payment/confirm', data),
  approve: (data) => api.post('/api/order/payment/approve', data),
  fail: (data) => api.post('/api/order/payment/fail', data),
}
