import api from '@/api/axios'

export const categoryApi = {
  getTopCategories: () => api.get('/api/category/top'),
  getSubCategories: (topId) => api.get(`/api/category/${topId}/sub`),
}
