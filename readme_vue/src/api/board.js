import api from './axios';

export const noticeApi = {
  getList: (params) => api.get('/api/notice', { params }),
  getDetail: (noticeId) => api.get(`/api/notice/${noticeId}`),
};

export const qnaApi = {
  getList: (params) => api.get('/api/qna', { params }),
  getDetail: (qnaId) => api.get(`/api/qna/${qnaId}`),
  create: (data) => api.post('/api/qna', data),
};

export const reviewApi = {
  getList: (params) => api.get('/api/review', { params }),
  getDetail: (reviewId) => api.get(`/api/review/${reviewId}`),
  react: (reviewId, reactionType) => api.post(`/api/review/${reviewId}/reaction`, { reactionType }),
};
