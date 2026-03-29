import api from "./axios";

export const authApi = {
  signup:           (data) => api.post('/api/auth/signup', data),
  signin:           (data) => api.post('/api/auth/signin', data),
  signout:          ()     => api.post('/api/auth/signout'),
  refresh:          (data) => api.post('/api/auth/refresh', data),
  getKakaoAuthUrl:  ()     => api.get('/api/oauth/kakao/authorize'),
};