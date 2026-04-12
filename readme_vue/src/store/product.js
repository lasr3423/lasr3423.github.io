import { defineStore } from 'pinia';
import { ref } from 'vue';
import { productApi } from '@/api/product';

export const useProductStore = defineStore('product', () => {
  const products = ref([]);
  const selectedTopId = ref(null);
  const selectedSubId = ref(null);
  const keyword = ref('');

  const currentPage = ref(0);
  const totalPages = ref(0);
  const totalElements = ref(0);
  const pageSize = ref(12);

  const loading = ref(false);
  const error = ref(null);

  async function fetchProducts(
    page = 0,
    topId = selectedTopId.value,
    subId = selectedSubId.value,
    nextKeyword = keyword.value,
    options = {},
  ) {
    selectedTopId.value = topId ?? null;
    selectedSubId.value = subId ?? null;
    keyword.value = nextKeyword?.trim?.() ?? '';

    loading.value = true;
    error.value = null;

    try {
      const sortField = options.sortField || 'createdAt';
      const sortDirection = options.sortDirection || 'desc';
      const params = {
        page,
        size: pageSize.value,
        sort: `${sortField},${sortDirection}`,
      };

      if (selectedTopId.value !== null) params.categoryTopId = selectedTopId.value;
      if (selectedSubId.value !== null) params.categorySubId = selectedSubId.value;
      if (keyword.value) params.keyword = keyword.value;

      const { data } = await productApi.getList(params);
      products.value = data.content;
      currentPage.value = data.number;
      totalPages.value = data.totalPages;
      totalElements.value = data.totalElements;
    } catch (e) {
      error.value = '상품을 불러오지 못했습니다.';
      console.error(e);
    } finally {
      loading.value = false;
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
    keyword,
    fetchProducts,
  };
});
