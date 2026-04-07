import { defineStore } from "pinia";
import { ref } from 'vue'
import { productApi } from '@/api/product'

export const useProductStore = defineStore('product', () => {

    // 상품 목록 데이터
    const products = ref([])

    // 현재 선택된 카테고리 상태를 store에서 관리
    const selectedTopId = ref(null)
    const selectedSubId = ref(null)

    // 페이징 정보
    const currentPage = ref(0)  // 현재 페이지 (0부터 시작)
    const totalPages = ref(0)   // 전체 페이지 수
    const totalElements = ref(0)    // 전체 상품 수
    const pageSize = ref(12)    // 한 페이지에 12개씩

    // 로딩 / 에러 상태
    const loading = ref(false)
    const error = ref(null)

    // 상품 목록 가져오기
    async function fetchProducts(page = 0, topId = null, subId = null) {

        // 파라미터가 명시적으로 넘어오면 store 상태 업데이트
        // null이 아닌 경우만 덮어씀 (페이지 이동 시 기존 필터 유지용)
        if (topId !== undefined) selectedTopId.value = topId
        if (subId !== undefined) selectedSubId.value = subId

        loading.value = true
        error.value = null


        try {
            const params = {
                page,
                size: pageSize.value,
                sort: 'createdAt,desc'
            }

            // null 아닐 때만 파라미터에 추가(백엔드에서 required=false로 받아옴)
            if (selectedTopId.value !== null) params.categoryTopId = selectedTopId.value
            if (selectedSubId.value !== null) params.categorySubId = selectedSubId.value

            const { data } = await productApi.getList(params)
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
        selectedTopId,
        selectedSubId,
        fetchProducts
    }

})