import api from './axios'

export const adminApi = {
  getDashboard: () => api.get('/api/admin/dashboard'),

  getMembers: (params) => api.get('/api/admin/members', { params }),
  getMember: (id) => api.get(`/api/admin/members/${id}`),
  updateMemberStatus: (id, status) => api.patch(`/api/admin/members/${id}/status`, null, { params: { status } }),
  updateMemberRole: (id, role) => api.patch(`/api/admin/members/${id}/role`, null, { params: { role } }),

  getProducts: (params) => api.get('/api/admin/products', { params }),
  getProduct: (id) => api.get(`/api/admin/products/${id}`),
  lookupBookByIsbn: (isbn) => api.get('/api/admin/products/isbn', { params: { isbn } }),
  createProduct: (data) => api.post('/api/admin/products', data),
  updateProduct: (id, data) => api.put(`/api/admin/products/${id}`, data),
  deleteProduct: (id) => api.delete(`/api/admin/products/${id}`),
  updateStock: (id, stock) => api.patch(`/api/admin/products/${id}/stock`, null, { params: { stock } }),
  updateProductStatus: (id, status) => api.patch(`/api/admin/products/${id}/status`, null, { params: { status } }),
  uploadProductThumbnail: (formData) => api.post('/api/admin/uploads/products', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),

  getOrders: (params) => api.get('/api/admin/orders', { params }),
  getOrder: (id) => api.get(`/api/admin/orders/${id}`),
  updateOrderStatus: (id, status) => api.patch(`/api/admin/orders/${id}/status`, null, { params: { status } }),
  updateOrderStatuses: (orderIds, status) => api.patch('/api/admin/orders/status/bulk', { orderIds, status }),
  getPendingApprovalOrders: (params) => api.get('/api/admin/orders/pending-approval', { params }),

  getDeliveries: (params) => api.get('/api/admin/deliveries', { params }),
  updateDelivery: (id, data) => api.patch(`/api/admin/deliveries/${id}`, data),

  getPayments: (params) => api.get('/api/admin/payments', { params }),
  getPayment: (id) => api.get(`/api/admin/payments/${id}`),

  getCategories: () => api.get('/api/admin/categories'),
  createCategoryTop: (data) => api.post('/api/admin/categories/top', data),
  createCategorySub: (data) => api.post('/api/admin/categories/sub', data),
  updateCategoryTop: (id, data) => api.put(`/api/admin/categories/top/${id}`, data),
  updateCategorySub: (id, data) => api.put(`/api/admin/categories/sub/${id}`, data),
  deleteCategoryTop: (id) => api.delete(`/api/admin/categories/top/${id}`),
  deleteCategorySub: (id) => api.delete(`/api/admin/categories/sub/${id}`),

  getAdminNotices: (params) => api.get('/api/admin/notices', { params }),
  getAdminNotice: (id) => api.get(`/api/admin/notices/${id}`),
  createNotice: (data) => api.post('/api/admin/notices', data),
  updateNotice: (id, data) => api.put(`/api/admin/notices/${id}`, data),
  deleteNotice: (id) => api.delete(`/api/admin/notices/${id}`),

  getAdminQnas: (params) => api.get('/api/admin/qnas', { params }),
  getAdminQna: (id) => api.get(`/api/admin/qnas/${id}`),
  answerQna: (id, content, title = '관리자 답변') => api.post(`/api/admin/qnas/${id}/answer`, { title, content }),
  updateAnswerQna: (id, content, title = '관리자 답변') => api.put(`/api/admin/qnas/${id}/answer`, { title, content }),
  deleteAnswerQna: (id) => api.delete(`/api/admin/qnas/${id}/answer`),
  updateQnaStatus: (id, status) => api.patch(`/api/admin/qnas/${id}/status`, null, { params: { status } }),

  getAdminReviews: (params) => api.get('/api/admin/reviews', { params }),
  getAdminReview: (id) => api.get(`/api/admin/reviews/${id}`),
  deleteReview: (id) => api.delete(`/api/admin/reviews/${id}`),
}
