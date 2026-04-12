import api from './axios';

export const noticeApi = {
  getList: (params) => api.get('/api/notice', { params }),
  getDetail: (noticeId) => api.get(`/api/notice/${noticeId}`),
};

export const qnaApi = {
  getList: (params) => api.get('/api/qna', { params }),
  getDetail: (qnaId) => api.get(`/api/qna/${qnaId}`),
  create: (data) => api.post('/api/qna', data),
  update: (qnaId, data) => api.put(`/api/qna/${qnaId}`, data),
  remove: (qnaId) => api.delete(`/api/qna/${qnaId}`),
  getMyList: (params) => api.get('/api/member/me/qnas', { params }),
  getMyDetail: (qnaId) => api.get(`/api/member/me/qnas/${qnaId}`),
  updateMy: (qnaId, data) => api.put(`/api/member/me/qnas/${qnaId}`, data),
  removeMy: (qnaId) => api.delete(`/api/member/me/qnas/${qnaId}`),
};

export const reviewApi = {
  getRecent: (params) => api.get('/api/review/recent', { params }),
  getList: (params) => api.get('/api/review', { params }),
  getDetail: (reviewId) => api.get(`/api/review/${reviewId}`),
  create: (data) => api.post('/api/review', data),
  update: (reviewId, data) => api.put(`/api/review/${reviewId}`, data),
  remove: (reviewId) => api.delete(`/api/review/${reviewId}`),
  react: (reviewId, reactionType) => api.post(`/api/review/${reviewId}/reaction`, { reactionType }),
  getMyList: (params) => api.get('/api/member/me/reviews', { params }),
  getMyDetail: (reviewId) => api.get(`/api/member/me/reviews/${reviewId}`),
  updateMy: (reviewId, data) => api.put(`/api/member/me/reviews/${reviewId}`, data),
  removeMy: (reviewId) => api.delete(`/api/member/me/reviews/${reviewId}`),
};
