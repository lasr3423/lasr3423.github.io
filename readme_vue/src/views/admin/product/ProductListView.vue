<template>
  <div class="space-y-6">

    <!-- 헤더 -->
    <section class="flex flex-col gap-4 rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 목록 관리</h1>
        <p class="mt-1 text-sm text-slate-400">재고·상태 변경은 <router-link to="/admin/product/stock" class="font-semibold text-brand-700 underline underline-offset-2">재고 관리</router-link> 페이지를 이용해 주세요.</p>
      </div>
      <router-link
        to="/admin/product/insert"
        class="inline-flex shrink-0 items-center justify-center rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
      >
        + 상품 등록
      </router-link>
    </section>

    <!-- 검색 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <input
          v-model.trim="keyword"
          type="text"
          placeholder="상품명을 입력하세요"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400 focus:bg-white"
          @keyup.enter="search"
        >
        <button
          type="button"
          class="shrink-0 rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="search"
        >
          검색
        </button>
      </div>
    </section>

    <!-- 목록 -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
        <h2 class="text-lg font-semibold text-slate-900">상품 목록</h2>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
          총 {{ totalElements.toLocaleString() }}건
        </span>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-20 text-sm text-slate-400">
        상품 목록을 불러오는 중입니다.
      </div>

      <template v-else-if="products.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 w-[40%]">상품</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">저자</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">카테고리</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">판매가</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="product in products" :key="product.id" class="transition hover:bg-slate-50">

                <!-- 상품 -->
                <td class="px-6 py-4">
                  <div class="flex items-center gap-4">
                    <img
                      :src="resolveAssetUrl(product.thumbnail)"
                      :alt="product.title"
                      class="h-16 w-12 shrink-0 rounded-xl border border-slate-200 object-cover"
                    >
                    <div class="min-w-0">
                      <p class="truncate font-semibold text-slate-900">{{ product.title }}</p>
                      <p class="mt-0.5 text-xs text-slate-400">ISBN {{ product.isbn || '-' }}</p>
                    </div>
                  </div>
                </td>

                <!-- 저자 -->
                <td class="px-6 py-4 text-slate-600">{{ product.author || '-' }}</td>

                <!-- 카테고리 -->
                <td class="px-6 py-4 text-slate-600">
                  <span v-if="product.categoryTopName">{{ product.categoryTopName }}</span>
                  <span v-if="product.categoryTopName && product.categorySubName" class="mx-1 text-slate-300">/</span>
                  <span v-if="product.categorySubName" class="text-slate-400">{{ product.categorySubName }}</span>
                  <span v-if="!product.categoryTopName && !product.categorySubName">-</span>
                </td>

                <!-- 판매가 -->
                <td class="px-6 py-4 text-right font-semibold text-slate-900">{{ formatPrice(product.salePrice) }}</td>

                <!-- 관리 버튼 -->
                <td class="px-6 py-4 text-right">
                  <div class="flex items-center justify-end gap-2">
                    <router-link
                      :to="`/admin/product/edit/${product.id}`"
                      class="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
                    >
                      수정
                    </router-link>
                    <button
                      type="button"
                      class="rounded-xl bg-rose-50 px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100"
                      @click="handleDelete(product.id)"
                    >
                      삭제
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 페이지네이션 -->
        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button
            type="button"
            :disabled="page === 0"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40"
            @click="changePage(page - 1)"
          >이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            type="button"
            :disabled="page >= totalPages - 1"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40"
            @click="changePage(page + 1)"
          >다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-20 text-sm text-slate-400">
        조건에 맞는 상품이 없습니다.
      </div>
    </section>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'

const products = ref([])
const loading = ref(true)
const keyword = ref('')
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)

const formatPrice = (value) => `${Number(value || 0).toLocaleString()}원`

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await adminApi.getProducts({
      page: page.value,
      size: 20,
      keyword: keyword.value || undefined,
    })
    products.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || products.value.length
  } catch (error) {
    console.error('상품 목록 조회 실패:', error)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 0
  fetchProducts()
}

async function handleDelete(productId) {
  if (!confirm('선택한 상품을 삭제하시겠습니까?')) return
  try {
    await adminApi.deleteProduct(productId)
    await fetchProducts()
  } catch (error) {
    alert(error.response?.data?.message || '상품 삭제에 실패했습니다.')
  }
}

async function changePage(nextPage) {
  page.value = nextPage
  await fetchProducts()
}

onMounted(fetchProducts)
</script>
