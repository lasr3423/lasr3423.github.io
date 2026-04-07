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

    // 주문 관리
    getOrders:          (params)        => api.get('/api/admin/orders', { params }),
    getOrder:           (id)            => api.get(`/api/admin/orders/${id}`),
    updateOrderStatus:  (id, status)    => api.patch(`/api/admin/orders/${id}/status`, null, { params: { status } }),

    // 배송 관리
    getDeliveries:      (params)        => api.get('/api/admin/deliveries', { params }),
    updateDelivery:     (id, data)      => api.patch(`/api/admin/deliveries/${id}`, data),
};
