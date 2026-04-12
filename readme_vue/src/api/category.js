import api from './axios';

export const categoryApi = {
  getTopCategories: () => api.get('/api/category/top'),
};
