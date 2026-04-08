import api from './axios';

export const memberApi = {
    // 내 정보 조회
    getMe:          ()       => api.get('/api/member/me'),
    // 정보 수정 (이름 / 전화 / 주소)
    updateMe:       (data)   => api.put('/api/member/me', data),
    // 비밀번호 변경
    changePassword: (data)   => api.put('/api/member/me/password', data),
    // 회원 탈퇴
    withdraw:       ()       => api.delete('/api/member/me'),
    // 내 주문 목록
    getOrders:      (params) => api.get('/api/member/me/orders', { params }),
    // 내 주문 상세
    getOrder:       (id)     => api.get(`/api/member/me/orders/${id}`),
    // 내 결제 내역
    getPayments:    (params) => api.get('/api/member/me/payments', { params }),
};
