import { defineStore } from "pinia";
import { ref } from 'vue'
import { productApi } from '@/api/product'

export const useProductStore = defineStore('product', () => {

    // 상품 목록 데이터
    const products = ref([])

    // 페이징 정보
    const currentPage = ref(0)  // 현재 페이지 (0부터 시작)
    const totalPages = ref(0)   // 전체 페이지 수
    const totalElements = ref(0)    // 전체 상품 수
    const pageSize = ref(12)    // 한 페이지에 12개씩

    // 로딩 / 에러 상태
    const loading = ref(false)
    const error = ref(null)

    // 상품 목록 가져오기
    async function fetchProducts(page = 0) {

        loading.value = true
        error.value = null

        try {
            const { data } = await productApi.getList({
                page,
                size: pageSize.value,
                sort: 'createdAt,desc'
            })
            products.value = data.content
            currentPage.value = data.number
            totalPages.value = data.totalPages
            totalElements.value = data.totalElements
        } catch (e) {
            error.value = '상품을 불러오지 못했습니다.'
            console.error(e)
        } finally {
            loading.value = false
        }
        
    }

    return {
        products,
        currentPage,
        totalPages,
        totalElements,
        pageSize,
        loading,
        error,
        fetchProducts
    }

})