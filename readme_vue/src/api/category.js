<<<<<<< HEAD
import api from '@/api/axios'

export const categoryApi = {
  getTopCategories: () => api.get('/api/category/top'),
  getSubCategories: (topId) => api.get(`/api/category/${topId}/sub`),
}
=======
import api from './axios';

export const categoryApi = {
  getTopCategories: () => api.get('/api/category/top'),
};
>>>>>>> 1bf975f37ccd4fd92a80f2cce73d7f4a28eb74c1
