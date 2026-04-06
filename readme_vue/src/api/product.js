import api from '@/api/axios'

export const productApi = {

    getList: (params) => api.get('/api/product', {params}),

    getDetail: (productId) => api.get(`/api/product/detail/${productId}`)

} 