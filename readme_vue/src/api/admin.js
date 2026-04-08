import api from './axios';

export const adminApi = {
    // 대시보드
    getDashboard: () => api.get('/api/admin/dashboard'),

    // 회원 관리
    getMembers:         (params)        => api.get('/api/admin/members', { params }),
    getMember:          (id)            => api.get(`/api/admin/members/${id}`),
    updateMemberStatus: (id, status)    => api.patch(`/api/admin/members/${id}/status`, null, { params: { status } }),
    updateMemberRole:   (id, role)      => api.patch(`/api/admin/members/${id}/role`,   null, { params: { role } }),

    // 상품 관리
    getProducts:    (params)        => api.get('/api/admin/products', { params }),
    getProduct:     (id)            => api.get(`/api/admin/products/${id}`),
    createProduct:  (data)          => api.post('/api/admin/products', data),
    updateProduct:  (id, data)      => api.put(`/api/admin/products/${id}`, data),
    deleteProduct:  (id)            => api.delete(`/api/admin/products/${id}`),
    updateStock:    (id, stock)     => api.patch(`/api/admin/products/${id}/stock`, null, { params: { stock } }),

    // 주문 관리
    getOrders:              (params)        => api.get('/api/admin/orders', { params }),
    getOrder:               (id)            => api.get(`/api/admin/orders/${id}`),
    updateOrderStatus:      (id, status)    => api.patch(`/api/admin/orders/${id}/status`, null, { params: { status } }),
    getPendingApprovalOrders: (params)      => api.get('/api/admin/orders/pending-approval', { params }),

    // 배송 관리
    getDeliveries:      (params)        => api.get('/api/admin/deliveries', { params }),
    updateDelivery:     (id, data)      => api.patch(`/api/admin/deliveries/${id}`, data),

    // 결제 내역 관리
    getPayments:    (params)        => api.get('/api/admin/payments', { params }),

    // 카테고리 관리
    getCategories:  ()              => api.get('/api/admin/categories'),

    // 공지사항 관리
    getAdminNotices: (params)       => api.get('/api/admin/notices', { params }),
    createNotice:    (data)         => api.post('/api/notice', data),
    updateNotice:    (id, data)     => api.put(`/api/notice/${id}`, data),
    deleteNotice:    (id)           => api.delete(`/api/notice/${id}`),

    // QnA 관리
    getAdminQnas:   (params)        => api.get('/api/admin/qnas', { params }),
    answerQna:      (id, answer)    => api.post(`/api/qna/${id}/answer`, { answer }),

    // 리뷰 관리
    getAdminReviews: (params)       => api.get('/api/admin/reviews', { params }),
    deleteReview:    (id)           => api.delete(`/api/admin/reviews/${id}`),
};
