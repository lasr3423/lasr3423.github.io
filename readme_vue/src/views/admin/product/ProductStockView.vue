<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">재고 관리</h1>
      <p class="mt-1 text-sm text-slate-400">상품별 재고 수량을 직접 변경합니다. 재고 0이면 자동 비활성화됩니다.</p>
    </section>

    <!-- 검색 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <input v-model="keyword" type="text" placeholder="상품명 검색"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          @keyup.enter="search" />
        <button @click="search"
          class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700">
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
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">현재 재고</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">재고 변경</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="p in products" :key="p.productId" class="transition hover:bg-slate-50">
                <td class="px-6 py-4">
                  <p class="font-semibold text-slate-800 line-clamp-1">{{ p.title }}</p>
                  <p class="text-xs text-slate-400">{{ p.author }}</p>
                </td>
                <td class="px-6 py-4 text-center">
                  <span :class="p.productStatus === 'ACTIVATE' ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'"
                    class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ p.productStatus === 'ACTIVATE' ? '판매중' : '비활성' }}
                  </span>
                </td>
                <td class="px-6 py-4 text-center font-bold"
                  :class="p.stock === 0 ? 'text-rose-500' : p.stock < 10 ? 'text-amber-500' : 'text-slate-800'">
                  {{ p.stock }}
                </td>
                <td class="px-6 py-4 text-center">
                  <div class="flex items-center justify-center gap-2">
                    <input
                      v-model.number="stockInput[p.productId]"
                      type="number" min="0"
                      class="w-20 rounded-xl border border-slate-200 px-2 py-1 text-center text-sm outline-none focus:border-brand-400"
                      :placeholder="String(p.stock)"
                    />
                    <button
                      class="rounded-xl bg-brand-800 px-3 py-1.5 text-xs font-semibold text-white hover:bg-brand-700"
                      @click="updateStock(p.productId, p)"
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
          <button :disabled="page === 0" @click="page--; fetchProducts()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchProducts()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
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
const stockInput = ref({})

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await adminApi.getProducts({ page: page.value, size: 20, keyword: keyword.value || undefined })
    products.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    // 입력 초기화
    products.value.forEach(p => { stockInput.value[p.productId] = p.stock })
  } catch (e) {
    console.error('상품 목록 로드 실패', e)
  } finally {
    loading.value = false
  }
}

function search() { page.value = 0; fetchProducts() }

async function updateStock(productId, product) {
  const val = stockInput.value[productId]
  if (val === undefined || val === null || val < 0) return alert('0 이상의 숫자를 입력하세요.')
  try {
    await adminApi.updateStock(productId, val)
    product.stock = val
    product.productStatus = val === 0 ? 'DEACTIVATE' : 'ACTIVATE'
    alert('재고가 변경되었습니다.')
  } catch (e) {
    alert(e.response?.data?.message || '재고 변경 실패')
  }
}

onMounted(fetchProducts)
</script>
