<template>
  <div class="space-y-6">
    <section class="flex flex-col gap-4 rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm lg:flex-row lg:items-center lg:justify-between">
      <div>
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 목록 관리</h1>
        <p class="mt-2 text-sm leading-6 text-slate-500">
          상품 검색과 상태 점검을 먼저 진행하고, 수정이 필요한 상품은 별도 수정 페이지에서 관리합니다.
        </p>
      </div>
      <router-link
        to="/admin/product/insert"
        class="inline-flex items-center justify-center rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
      >
        + 상품 등록
      </router-link>
    </section>

    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-slate-900">검색 및 필터</h2>
          <p class="mt-1 text-sm text-slate-500">상품명 검색과 상태 필터로 원하는 상품을 빠르게 찾을 수 있습니다.</p>
        </div>

        <div class="grid gap-3 md:grid-cols-[240px_180px_180px_auto]">
          <label class="space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">상품명</span>
            <input
              v-model.trim="keyword"
              type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400"
              placeholder="상품명을 입력하세요"
              @keyup.enter="search"
            >
          </label>

          <label class="space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">판매 상태</span>
            <select
              v-model="statusFilter"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400"
            >
              <option value="">전체 상태</option>
              <option value="ACTIVATE">판매 중</option>
              <option value="DEACTIVATE">판매 중단</option>
              <option value="DELETE">삭제</option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">재고 상태</span>
            <select
              v-model="stockFilter"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400"
            >
              <option value="">전체 재고</option>
              <option value="LOW">재고 부족</option>
            </select>
          </label>

          <button
            type="button"
            class="inline-flex h-[46px] items-center justify-center rounded-2xl bg-brand-800 px-5 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="search"
          >
            검색
          </button>
        </div>
      </div>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
        <div>
          <h2 class="text-lg font-semibold text-slate-900">상품 목록</h2>
          <p class="mt-1 text-sm text-slate-500">수정 버튼을 누르면 상품 수정 페이지로 이동합니다.</p>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
          총 {{ totalElements.toLocaleString() }}건
        </span>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-20 text-sm text-slate-400">
        상품 목록을 불러오는 중입니다.
      </div>

      <template v-else-if="filteredProducts.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full min-w-[1080px] text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">상품</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">대분류</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">소분류</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">저자</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">판매가</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">재고</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="product in filteredProducts" :key="product.id" class="transition hover:bg-slate-50">
                <td class="px-6 py-4">
                  <div class="flex items-center gap-4">
                    <img
                      :src="resolveAssetUrl(product.thumbnail)"
                      :alt="product.title"
                      class="h-16 w-12 rounded-xl border border-slate-200 object-cover"
                    >
                    <div class="min-w-0">
                      <p class="truncate font-semibold text-slate-900">{{ product.title }}</p>
                      <p class="mt-1 text-xs text-slate-400">ISBN {{ product.isbn || '-' }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 text-slate-600">{{ product.categoryTopName || '-' }}</td>
                <td class="px-6 py-4 text-slate-600">{{ product.categorySubName || '-' }}</td>
                <td class="px-6 py-4 text-slate-600">{{ product.author }}</td>
                <td class="px-6 py-4 text-right text-slate-700">{{ formatPrice(product.salePrice) }}</td>
                <td class="px-6 py-4 text-right">
                  <span :class="product.stock < 10 ? 'font-semibold text-rose-600' : 'text-slate-700'">{{ product.stock }}</span>
                </td>
                <td class="px-6 py-4 text-center">
                  <span :class="statusClass(product.productStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ statusLabel(product.productStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <div class="flex justify-end gap-2">
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

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button type="button" :disabled="page === 0" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="changePage(page - 1)">
            이전
          </button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button type="button" :disabled="page >= totalPages - 1" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="changePage(page + 1)">
            다음
          </button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-20 text-sm text-slate-400">
        조건에 맞는 상품이 없습니다.
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'

const products = ref([])
const loading = ref(true)
const statusFilter = ref('')
const stockFilter = ref('')
const keyword = ref('')
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)

const filteredProducts = computed(() => {
  if (stockFilter.value !== 'LOW') return products.value
  return products.value.filter((product) => product.stock < 10)
})

const statusLabel = (status) => ({ ACTIVATE: '판매 중', DEACTIVATE: '판매 중단', DELETE: '삭제' }[status] ?? status)
const statusClass = (status) => ({
  ACTIVATE: 'bg-green-50 text-green-700',
  DEACTIVATE: 'bg-yellow-50 text-yellow-700',
  DELETE: 'bg-rose-50 text-rose-700',
}[status] ?? 'bg-slate-100 text-slate-600')
const formatPrice = (value) => `${Number(value || 0).toLocaleString()}원`

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await adminApi.getProducts({
      page: page.value,
      size: 20,
      status: statusFilter.value || undefined,
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
