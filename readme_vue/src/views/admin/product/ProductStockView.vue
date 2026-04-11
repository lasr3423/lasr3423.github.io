<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">재고 / 상태 관리</h1>
      <p class="mt-1 text-sm text-slate-400">재고 수량 변경 및 판매 상태(판매 중 / 판매 중단)를 직접 관리합니다.</p>
    </section>

    <!-- 검색 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <input
          v-model="keyword"
          type="text"
          placeholder="상품명 검색"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400 focus:bg-white"
          @keyup.enter="search"
        >
        <button
          class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="search"
        >
          검색
        </button>
      </div>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="products.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">상품명</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">판매 상태</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">현재 재고</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">재고 변경</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="p in products" :key="p.id" class="transition hover:bg-slate-50">

                <!-- 상품명 -->
                <td class="px-6 py-4">
                  <p class="font-semibold text-slate-800 line-clamp-1">{{ p.title }}</p>
                  <p class="text-xs text-slate-400">{{ p.author }}</p>
                </td>

                <!-- 판매 상태 + 토글 버튼 -->
                <td class="px-6 py-4 text-center">
                  <div class="flex flex-col items-center gap-2">
                    <span
                      class="rounded-full px-3 py-1 text-xs font-semibold"
                      :class="{
                        'bg-emerald-50 text-emerald-700': p.productStatus === 'ACTIVATE',
                        'bg-amber-50 text-amber-700':    p.productStatus === 'DEACTIVATE',
                        'bg-rose-50 text-rose-700':      p.productStatus === 'DELETE',
                      }"
                    >
                      {{ { ACTIVATE: '판매 중', DEACTIVATE: '판매 중단', DELETE: '삭제' }[p.productStatus] ?? p.productStatus }}
                    </span>
                    <button
                      v-if="p.productStatus !== 'DELETE'"
                      class="rounded-xl border px-3 py-1 text-xs font-semibold transition"
                      :class="p.productStatus === 'ACTIVATE'
                        ? 'border-amber-200 text-amber-700 hover:bg-amber-50'
                        : 'border-emerald-200 text-emerald-700 hover:bg-emerald-50'"
                      @click="toggleStatus(p)"
                    >
                      {{ p.productStatus === 'ACTIVATE' ? '판매 중단' : '판매 재개' }}
                    </button>
                  </div>
                </td>

                <!-- 현재 재고 -->
                <td
                  class="px-6 py-4 text-center font-bold"
                  :class="p.stock === 0 ? 'text-rose-500' : p.stock < 10 ? 'text-amber-500' : 'text-slate-800'"
                >
                  {{ p.stock }}
                </td>

                <!-- 재고 변경 (행마다 독립 입력값) -->
                <td class="px-6 py-4 text-center">
                  <div class="flex items-center justify-center gap-2">
                    <input
                      v-model.number="p.editStock"
                      type="number"
                      min="0"
                      class="w-20 rounded-xl border border-slate-200 px-2 py-1 text-center text-sm outline-none focus:border-brand-400"
                    >
                    <button
                      class="rounded-xl bg-brand-800 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-brand-700"
                      @click="updateStock(p)"
                    >
                      저장
                    </button>
                  </div>
                </td>

              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="page--; fetchProducts()">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="page++; fetchProducts()">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">상품이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const products   = ref([])
const loading    = ref(true)
const keyword    = ref('')
const page       = ref(0)
const totalPages = ref(1)

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await adminApi.getProducts({ page: page.value, size: 20, keyword: keyword.value || undefined })
    // 각 상품에 editStock 독립 입력값 추가
    products.value = (data.content ?? []).map(p => ({ ...p, editStock: p.stock }))
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('상품 목록 로드 실패', e)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 0
  fetchProducts()
}

async function updateStock(product) {
  const val = product.editStock
  if (val === undefined || val === null || val < 0) {
    alert('0 이상의 숫자를 입력하세요.')
    return
  }
  try {
    await adminApi.updateStock(product.id, val)
    product.stock = val
    // 재고 0이면 자동 비활성화 반영
    if (val === 0) product.productStatus = 'DEACTIVATE'
    else if (product.productStatus === 'DEACTIVATE') product.productStatus = 'ACTIVATE'
    alert('재고가 변경되었습니다.')
  } catch (e) {
    alert(e.response?.data?.message || '재고 변경 실패')
  }
}

async function toggleStatus(product) {
  const next = product.productStatus === 'ACTIVATE' ? 'DEACTIVATE' : 'ACTIVATE'
  const label = next === 'ACTIVATE' ? '판매 재개' : '판매 중단'
  if (!confirm(`'${product.title}' 상품을 ${label} 처리하시겠습니까?`)) return
  try {
    await adminApi.updateProductStatus(product.id, next)
    product.productStatus = next
  } catch (e) {
    alert(e.response?.data?.message || '상태 변경 실패')
  }
}

onMounted(fetchProducts)
</script>
